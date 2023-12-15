
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

(defn read-header-block
  ; @ignore
  ;
  ; @param (strings in vector) header-block
  ;
  ; @example
  ; (read-header-block ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:type :param :meta ["map" "opt"] :value "my-map" :additional [" {...}"] :indent 1}
  ;
  ; @return (map)
  [header-block]
  (letfn [(f0 [       %] (keyword (regex/re-first % #"(?<=\;[\s\t]{0,}\@)[a-z]{1,}"))) ; <- Returns the header block type derived from the given comment row.
          (f1 [       %]          (regex/re-all   % #"(?<=\()[^()]+(?=\))"))           ; <- Returns the header block meta values (if any) derived from the given comment row.
          (f2 [       %]          (regex/re-last  % #"(?<=\@.+[\s\t])[^\s\t\(\)]+$"))  ; <- Returns the header block value (if any) derived from the given comment row.
          (f3 [       %] (count   (regex/re-first % #"(?<=\;)[\s\t]{0,}(?=\@)")))      ; <- Returns the header block indent length derived from the given comment row.
          (f4 [result %] (string/keep-range % (-> result :indent inc)))                ; <- Returns the given header block additional row with adjusted indent.
          (f5 [result %] (if (-> result empty?)                                                 ; <- The 'result' vector is empty when the iteration reads the first row of header block.
                             (-> result (merge {:type (f0 %) :indent (f3 %)}                    ; <- Every imported header block stars with a type row.
                                               (if-let [meta  (f1 %)] {:meta  meta})            ; <- Meta values of header blocks are optional.
                                               (if-let [value (f2 %)] {:value value})))         ; <- Values of header blocks are optional.
                             (-> result (update :additional vector/conj-item (f4 result %)))))] ; <- Rows after the first row (type row) are the header block additional rows.
         (reduce f5 {} header-block)))

(defn read-header-blocks
  ; @ignore
  ;
  ; @param (strings in vectors in vector) header
  ;
  ; @example
  ; (read-header-blocks [["; @param (map)(opt) my-map"  "; {...}"]
  ;                      ["; @param (vector) my-vector" "; [...]"]])
  ; =>
  ; [{:type :param :meta ["map" "opt"] :value "my-map"    :additional [" {...}"]}
  ;  {:type :param :meta ["vector"]    :value "my-vector" :additional [" [...]"]}]
  ;
  ; @return (maps in vector)
  [header]
  (vector/->items header read-header-block))

(defn split-header
  ; @ignore
  ;
  ; @param (strings in vector) header
  ;
  ; @example
  ; (split-header ["; @param (map) my-map" "; {...}" "; @param (vector) my-vector" "; [...]"])
  ; =>
  ; [["; @param (map) my-map"       "; {...}"]
  ;  ["; @param (vector) my-vector" "; [...]"]]
  ;
  ; @return (strings in vectors in vector)
  [header]
  (letfn [(f0 [%] (regex/re-match? % #"^[\s\t]{0,}\;[\s\t]{0,}\@")) ; <- Returns TRUE if the given comment row is a header block type row.
          (f1 [%] (regex/re-match? % #"^[\s\t]{0,}\;[\s\t]{0,}$"))  ; <- Returns TRUE if the given comment row is an empty comment row.
          (f2 [%] (vector/empty?   %))                              ; <- Returns TRUE if the given result vector is empty (no block has been opened yet).
          (f3 [result row-content]
              (cond (-> row-content f0)  (-> result (vector/conj-item [               row-content]))
                    (-> row-content f1)  (-> result (vector/conj-item ["; @separator" row-content]))
                    (-> result      f2)  (-> result (vector/conj-item ["; @undefined" row-content]))
                    :additional-row      (-> result (vector/update-last-item vector/conj-item row-content))))]
         (reduce f3 [] header)))

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
  (-> declaration (update :header split-header)
                  (update :header read-header-blocks)))

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
