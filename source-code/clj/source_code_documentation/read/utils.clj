
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [fruits.vector.api :as vector]
              [syntax-reader.api :as syntax-reader]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @type (meta)(meta) value
; additional
; =>
; {:type :type
;  :meta ["meta" "meta"]
;  :value "value"
;  :additional ["additional"]}

; @param (map)(opt) options
; {:return? (boolean)(opt)
;   Default: false}
; =>
; {:type :param
;  :meta ["map" "opt"]}
;  :value "options"
;  :additional ["{:return? (boolean)(opt)" "  Default: false}"]

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-declaration-body
  ; @ignore
  ;
  ; @param (map) declaration
  ;
  ; @example
  ; (read-declaration-body {:name "my-function" :body "(defn my-function [])"})
  ; =>
  ; {:name "my-function"
  ;  :body "(defn my-function [])"}
  ;
  ; @return (map)
  [declaration]
  (letfn [(f0 [%] (syntax-reader/remove-tags % [[:comment #"\;" #"\n"] [:string  #"\"" #"\"" {:keep? true}]]
                                               {:keep-indents? true :remove-leftover-blank-lines? true}))]
         (update declaration :body f0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-doc-block
  ; @ignore
  ;
  ; @param (strings in vector) doc-block
  ;
  ; @example
  ; (read-doc-block ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:type :param :meta ["map" "opt"] :value "my-map" :additional [" {...}"] :indent 1}
  ;
  ; @return (map)
  [doc-block]
  (letfn [(f0 [       %] (keyword (regex/re-first % #"(?<=\;[ \t]{0,}\@)[a-z]{1,}"))) ; <- Returns the block type, derived from the given comment row.
          (f1 [       %]          (regex/re-all   % #"(?<=\()[^()]+(?=\))"))          ; <- Returns the block meta values (if any), derived from the given comment row.
          (f2 [       %]          (regex/re-last  % #"(?<=\@.+[ \t])[^\(\)]+$"))      ; <- Returns the block value (if any), derived from the given comment row.
          (f3 [       %] (count   (regex/re-first % #"(?<=\;)[ \t]{0,}(?=\@)")))      ; <- Returns the block indent length, derived from the given comment row.
          (f4 [result %] (string/keep-range % (-> result :indent inc)))               ; <- Returns the given block additional row, with adjusted indent.
          (f5 [result %] (if (-> result empty?)                                                 ; <- The 'result' vector is empty when the iteration reads the first row of block.
                             (-> result (merge {:type (f0 %) :indent (f3 %)}                    ; <- Every imported block starts with a type row.
                                               (let [meta  (f1 %)] (if (-> meta  vector/nonempty?) {:meta  meta}))    ; <- Meta values of blocks are optional.
                                               (let [value (f2 %)] (if (-> value string/nonempty?) {:value value})))) ; <- Values of blocks are optional.
                             (-> result (update :additional vector/conj-item (f4 result %)))))] ; <- Rows following the first row (type row) are the additional rows of the block.
         (reduce f5 {} doc-block)))

(defn read-doc-blocks
  ; @ignore
  ;
  ; @param (strings in vectors in vector) doc-blocks
  ;
  ; @example
  ; (read-doc-blocks [["; @param (map)(opt) my-map"  "; {...}"]
  ;                   ["; @param (vector) my-vector" "; [...]"]])
  ; =>
  ; [{:type :param :meta ["map" "opt"] :value "my-map"    :additional [" {...}"]}
  ;  {:type :param :meta ["vector"]    :value "my-vector" :additional [" [...]"]}]
  ;
  ; @return (maps in vector)
  [doc-blocks]
  (vector/->items doc-blocks read-doc-block))

(defn split-doc-blocks
  ; @ignore
  ;
  ; @param (strings in vector) doc-blocks
  ;
  ; @example
  ; (split-doc-blocks ["; @param (map) my-map" "; {...}" "; @param (vector) my-vector" "; [...]"])
  ; =>
  ; [["; @param (map) my-map"       "; {...}"]
  ;  ["; @param (vector) my-vector" "; [...]"]]
  ;
  ; @return (strings in vectors in vector)
  [doc-blocks]
  (letfn [(f0 [%] (-> % (regex/re-match? #"^[ \t]{0,}\;[ \t]{0,}\@"))) ; <- Returns TRUE if the given comment row is a block type row.
          (f1 [%] (-> % (regex/re-match? #"^[ \t]{0,}\;[ \t]{0,}$")))  ; <- Returns TRUE if the given comment row is an empty comment row.
          (f2 [%] (-> % last first (= "; @separator")))                ; <- Returns TRUE if the last block is a separator block.
          (f3 [%] (-> % vector/empty?))                                ; <- Returns TRUE if the given result vector is empty (no block has been opened yet).
          (f4 [result row-content]
              (cond (-> row-content f0) (-> result (vector/conj-item [               row-content]))           ; <- If the row content is a block type row, opens a new block.
                    (-> row-content f1) (-> result (vector/conj-item ["; @separator" row-content]))           ; <- If the row content is an empty comment row, opens a new separator block.
                    (-> result      f3) (-> result (vector/conj-item ["; @plain"     row-content]))           ; <- If the row content is a plain text row and no block has been opened yet, opens a new plain block.
                    (-> result      f2) (-> result (vector/conj-item ["; @plain"     row-content]))           ; <- If the row content is a plain text row and the last block is a separator block, opens a new plain block.
                    :additional-row     (-> result (vector/update-last-item vector/conj-item row-content))))] ; <- If the row content is a plain text row appends it the last opened block.
         (reduce f4 [] doc-blocks)))

(defn remove-ignored-doc-blocks
  ; @ignore
  ;
  ; @param (maps in vector) doc-blocks
  ;
  ; @return (maps in vector)
  [doc-blocks]
  (letfn [(f0 [%] (= :ignore (:type %)))]
         (vector/before-first-match doc-blocks f0 {:return? true})))

(defn read-declaration-header
  ; @ignore
  ;
  ; @param (map) declaration
  ;
  ; @example
  ; (read-declaration-header {:name "my-function" :header ["; Row #1" "; @param (map)(opt) options" ";  {...}" ";" "; @return (map)"]})
  ; =>
  ; {:name   "my-function"
  ;  :header [{:type :param  :meta ["map" "opt"] :value "options" :additional [" {...}"]}
  ;           {:type :return :meta ["map"]}]}
  ;
  ; @return (map)
  [declaration]
  (-> declaration (update :header split-doc-blocks)
                  (update :header read-doc-blocks)
                  (update :header remove-ignored-doc-blocks)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-tutorial-content
  ; @ignore
  ;
  ; @param (map) tutorial
  ;
  ; @return (map)
  [tutorial]
  (-> tutorial (update :content split-doc-blocks)
               (update :content read-doc-blocks)
               (update :content remove-ignored-doc-blocks)))
