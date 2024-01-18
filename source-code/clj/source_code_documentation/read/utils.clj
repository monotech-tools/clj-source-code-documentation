
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api       :as regex]
              [fruits.string.api      :as string]
              [fruits.vector.api      :as vector]
              [syntax-interpreter.api :as syntax-interpreter]
              [syntax-reader.api      :as syntax-reader]))

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

(defn read-section-source-code
  ; @ignore
  ;
  ; @param (map) section
  ;
  ; @usage
  ; (read-section-source-code {:name "my-function" :type :defn :content [...] :source-code "(defn my-function [])"})
  ; =>
  ; {:name        "my-function"
  ;  :type        :defn
  ;  :content     [...]
  ;  :source-code "(defn my-function [])"}
  ;
  ; @return (map)
  [section]
  (letfn [(f0 [%] (syntax-reader/remove-tags % [(syntax-interpreter/with-options (:comment syntax-interpreter/CLJ-PATTERNS) {})
                                                (syntax-interpreter/with-options (:string  syntax-interpreter/CLJ-PATTERNS) {:keep? true})]
                                               {:keep-indents? true :remove-leftover-blank-lines? true}))]
         (-> section :type (case :def      (-> section (update :source-code f0))
                                 :defn     (-> section (update :source-code f0))
                                 :tutorial (-> section)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-content-block
  ; @ignore
  ;
  ; @param (strings in vector) content-block
  ;
  ; @usage
  ; (read-content-block ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:type :param :meta ["map" "opt"] :value "my-map" :additional [" {...}"] :indent 1}
  ;
  ; @return (map)
  [content-block]
  (letfn [(f0 [       %] (keyword (regex/re-first % #"(?<=\;[\h]*\@)[a-z]+")))  ; <- Returns the block type, derived from the given comment row.
          (f1 [       %]          (regex/re-all   % #"(?<=\()[^()]+(?=\))"))    ; <- Returns the block meta values (if any), derived from the given comment row.
          (f2 [       %]          (regex/re-last  % #"(?<=\@.+[\h])[^\(\)]+$")) ; <- Returns the block value (if any), derived from the given comment row.
          (f3 [       %] (count   (regex/re-first % #"(?<=\;)[\h]*(?=\@)")))    ; <- Returns the block indent length, derived from the given comment row.
          (f4 [result %] (string/keep-range % (-> result :indent inc)))         ; <- Returns the given block additional row, with adjusted indent.
          (f5 [result %] (if (-> result empty?)                                                 ; <- The 'result' vector is empty when the iteration reads the first row of block.
                             (-> result (merge {:type (f0 %) :indent (f3 %)}                    ; <- Every imported block starts with a block marker row.
                                               (let [meta  (f1 %)] (if (-> meta  vector/not-empty?) {:meta  meta}))    ; <- Meta values of blocks are optional.
                                               (let [value (f2 %)] (if (-> value string/not-empty?) {:value value})))) ; <- Values of blocks are optional.
                             (-> result (update :additional vector/conj-item (f4 result %)))))] ; <- Rows following the first row (block marker row) are additional rows of the block.
         (reduce f5 {} content-block)))

(defn read-content-blocks
  ; @ignore
  ;
  ; @param (strings in vectors in vector) content-blocks
  ;
  ; @usage
  ; (read-content-blocks [["; @param (map)(opt) my-map"  "; {...}"]
  ;                       ["; @param (vector) my-vector" "; [...]"]])
  ; =>
  ; [{:type :param :meta ["map" "opt"] :value "my-map"    :additional [" {...}"]}
  ;  {:type :param :meta ["vector"]    :value "my-vector" :additional [" [...]"]}]
  ;
  ; @return (maps in vector)
  [content-blocks]
  (vector/->items content-blocks read-content-block))

(defn split-content-blocks
  ; @ignore
  ;
  ; @param (strings in vector) content-blocks
  ;
  ; @usage
  ; (split-content-blocks ["; @param (map) my-map" "; {...}" "; @param (vector) my-vector" "; [...]"])
  ; =>
  ; [["; @param (map) my-map"       "; {...}"]
  ;  ["; @param (vector) my-vector" "; [...]"]]
  ;
  ; @return (strings in vectors in vector)
  [content-blocks]
  ; - Block start marker row: "; @usage", "; @param", etc.
  ; - Block end marker row:   "; @---"
  ; - Empty comment row:      "; "
  ; - Nonempty comment row:   "; abc..."
  (letfn [(f0  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*\@")))                           ; <- Returns TRUE if the given comment row is a block marker row.
          (f1  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*\@\-\-\-")))                     ; <- Returns TRUE if the given comment row is a block end marker row.
          (f2  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*$")))                            ; <- Returns TRUE if the given comment row is an empty comment row.
          (f3  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*[^\@\h]")))                      ; <- Returns TRUE if the given comment row is an nonempty comment row.
          (f4  [%] (-> % (regex/re-first  #"^[\h]*\;[\h]*\@.*")))                         ; <- Returns the given comment row if it is a block marker row.
          (f5  [%] (-> content-blocks (vector/keep-range %) (vector/first-result f4) f1)) ; <- Returns TRUE if the next block marker row will be a block end marker row.
          (f6  [%] (-> % last first (= "; @separator")))                                  ; <- Returns TRUE if the last block is a separator block.
          (f7  [%] (-> % empty?))                                                         ; <- Returns TRUE if no block has been opened yet.
          (f8  [result cursor row-content] (cond (f0 row-content)  (-> result (f9  cursor row-content))   ; <- Block start marker or end marker row
                                                 (f2 row-content)  (-> result (f10 cursor row-content))   ; <- Empty comment row
                                                 (f3 row-content)  (-> result (f11 cursor row-content)))) ; <- Nonempty comment row
          (f9  [result cursor row-content] (cond (f1 row-content)  (-> result)                            ; <- Block end marker row
                                                 :start-marker     (-> result (f13 cursor row-content)))) ; <- Block start marker row
          (f10 [result cursor row-content] (cond (f5 cursor)       (-> result (f12 cursor row-content))   ; <- Empty comment row + the next block marker row will be a block end marker row.
                                                 :else             (-> result (f13 cursor row-content)))) ; <- Empty comment row + the next block marker row will NOT be a block end marker row.
          (f11 [result cursor row-content] (cond (f6 result)       (-> result (f13 cursor row-content))   ; <- Nonempty comment row + last block is a separator block
                                                 (f7 result)       (-> result (f13 cursor row-content))   ; <- Nonempty comment row + no block has been opened yet
                                                 :any-block-opened (-> result (f12 cursor row-content)))) ; <- Nonempty comment row + last block is NOT a separator block
          (f12 [result cursor row-content] (cond (f6 result)       (-> result (f13 cursor row-content))   ; <- Appends to previous block + last block is a separator block
                                                 (f7 result)       (-> result (f13 cursor row-content))   ; <- Appends to previous block + no block has been opened yet
                                                 :any-block-opened (-> result (vector/update-last-item vector/conj-item row-content))))
          (f13 [result cursor row-content] (cond (f2 row-content)  (-> result (vector/conj-item ["; @separator" row-content]))   ; <- Opens a new block + empty comment row
                                                 (f3 row-content)  (-> result (vector/conj-item ["; @plain"     row-content]))   ; <- Opens a new block + nonempty comment row
                                                 (f0 row-content)  (-> result (vector/conj-item [               row-content]))))] ; <- Opens a new block + block marker row
         ; ...
         (reduce-kv f8 [] content-blocks)))

(defn remove-ignored-content-blocks
  ; @ignore
  ;
  ; @param (maps in vector) content-blocks
  ;
  ; @return (maps in vector)
  [content-blocks]
  (letfn [(f0 [%] (= :ignore (:type %)))]
         (vector/before-first-match content-blocks f0 {:return? true})))

(defn read-section-content
  ; @ignore
  ;
  ; @param (map) section
  ;
  ; @usage
  ; (read-section-content {:name "my-function" :type :defn :content ["; Row #1" "; @param (map)(opt) options" ";  {...}" ";" "; @return (map)"]})
  ; =>
  ; {:name    "my-function"
  ;  :type    :defn
  ;  :content [{:type :param  :meta ["map" "opt"] :value "options" :additional [" {...}"]}
  ;            {:type :return :meta ["map"]}]}
  ;
  ; @return (map)
  [section]
  (-> section (update :content split-content-blocks)
              (update :content read-content-blocks)
              (update :content remove-ignored-content-blocks)))
