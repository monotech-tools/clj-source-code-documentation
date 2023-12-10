
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @type (meta)(meta) value
; additional

; {:type :param
;  :meta ["map" "opt"]}
;  :value "options"
;  :additional ["{:return? (boolean)(opt)" "  Default: false}"]

(defn read-header-block
  ; @ignore
  ;
  ; @param (strings in vector) n
  ;
  ; @example
  ; (read-header-block ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:type :param :meta ["map" "opt"] :value "my-map" :additional [" {...}"] :indent 1}
  ;
  ; @return (map)
  [n]
  (letfn [(f0 [       %] (keyword (regex/re-first % #"(?<=\;[\s\t]{0,}\@)[a-z]{1,}"))) ; <- Returns the block type derived from the given comment row.
          (f1 [       %]          (regex/re-all   % #"(?<=\()[^()]+(?=\))"))           ; <- Returns the meta values (if any) derived from the given comment row.
          (f2 [       %]          (regex/re-first % #"(?<=[\s\t]{1,})[a-z]+$"))        ; <- Returns the block value (if any) derived from the given comment row.
          (f3 [       %] (count   (regex/re-first % #"(?<=\;)[\s\t]{0,}(?=\@)")))      ; <- Returns the block indent length derived from the given comment row.
          (f4 [result %] (:indent result) %) ; TODO
          (f5 [result %] (if (-> result empty?)
                             (-> result (merge {:type (f0 %) :indent (f3 %)}
                                               (if-let [meta  (f1 %)] {:meta  meta})
                                               (if-let [value (f2 %)] {:value value})))
                             (-> result (update :additional vector/conj-item (f4 result %)))))]
         (reduce f5 {} n)))

(defn read-header-blocks
  ; @ignore
  ;
  ; @param (strings in vectors in vector) n
  ;
  ; @example
  ; (read-header-blocks [["; @param (map)(opt) my-map"  "; {...}"]
  ;                      ["; @param (vector) my-vector" "; [...]"]])
  ; =>
  ; [{:type :param :meta ["map" "opt"] :value "my-map"    :additional [" {...}"]}
  ;  {:type :param :meta ["vector"]    :value "my-vector" :additional [" [...]"]}]
  ;
  ; @return (maps in vector)
  [n]
  (vector/->items n read-header-block))

(defn split-header-blocks
  ; @ignore
  ;
  ; @param (strings in vector) n
  ;
  ; @example
  ; (split-header-blocks ["; @param (map) my-map" "; {...}" "; @param (vector) my-vector" "; [...]"])
  ; =>
  ; [["; @param (map) my-map"       "; {...}"]
  ;  ["; @param (vector) my-vector" "; [...]"]]
  ;
  ; @return (strings in vectors in vector)
  [n]
  (letfn [(f0 [%] (regex/re-match? % #"^[\s\t]{0,}\;[\s\t]{0,}\@")) ; <- Returns TRUE if the given row is a block type row.
          (f1 [%] (regex/re-match? % #"^[\s\t]{0,}\;[\s\t]{0,}$"))  ; <- Returns TRUE if the given row is an empty comment row.
          (f2 [[in-block? result] row-content]
              (cond (-> row-content f0) [true  (-> result (vector/conj-item [row-content]))]
                    (-> row-content f1) [false (-> result)] ; <- An empty row always closes the previous block.
                    (-> in-block? not)  [false (-> result)] ; <- The row is ignored if no block is opened.
                    :else               [true  (-> result (vector/update-last-item vector/conj-item row-content))]))]
         (let [[_ result] (reduce f2 [false []] n)]
              (-> result))))

(defn read-header
  ; @ignore
  ;
  ; @param (strings in vector) n
  ;
  ; @return (maps in vector)
  [n]
  (-> n (split-header-blocks)
        (read-header-blocks)))
