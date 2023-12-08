
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
  ; {:type :param :meta ["map" "opt"] :value "my-map"    :additional [" {...}"]}
  ;
  ; @return (map)
  [n]
  (letfn [(f0 [       %] (regex/re-first  % #"(?<=\;[\s\t]{0,}\@)[a-z]{1,}"))   ; <-
          (f2 [       %] (regex/re-all    % #"(?<=\()[^()]+(?=\))"))            ; <- Returns all meta values
          (fx [       %] (regex/re-first  % #"(?<=[\s\t]{1,})[a-z]+$"))         ; <- Returns the value (if anyy)
          (fy [       %] (count (regex/re-first % #"(?<=\;)[\s\t]{0,}(?=\@)"))) ; <- Returns the block indent length
          (fz [indent %] %)
          (f4 [result %] (cond (-> result :type nil?) {:type (f0 %) :meta (f2 %) :value (fx %) :indent (fy %)}
                               :else (update result :additional vector/conj-item (fz (:indent result) %))))]
         (reduce f4 {} n)))

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
