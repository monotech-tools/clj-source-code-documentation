
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [fruits.vector.api :as vector]))

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
  ; @param (strings in vector) n
  ;
  ; @example
  ; (read-header-block ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:type :param :meta ["map" "opt"] :value "my-map" :additional [" {...}"] :indent 1}
  ;
  ; @return (map)
  [n]
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
  (letfn [(f0 [%] (regex/re-match? % #"^[\s\t]{0,}\;[\s\t]{0,}\@")) ; <- Returns TRUE if the given comment row is a header block type row.
          (f1 [%] (regex/re-match? % #"^[\s\t]{0,}\;[\s\t]{0,}$"))  ; <- Returns TRUE if the given comment row is an empty comment row.
          (f2 [[in-block? result] row-content]
              (cond (-> row-content f0) [true  (-> result (vector/conj-item [row-content]))] ; <- Header block type rows always starts a new header block.
                    (-> row-content f1) [false (-> result)]                                  ; <- An empty comment row always closes the previous block.
                    (-> in-block? not)  [false (-> result)]                                  ; <- The comment row is ignored if no header block is opened.
                    :additional-row     [true  (-> result (vector/update-last-item vector/conj-item row-content))]))]
         (let [[_ result] (reduce f2 [false []] n)]
              (-> result))))

(defn read-header
  ; @ignore
  ;
  ; @param (strings in vector) n
  ;
  ; @example
  ; (read-header ["; Row #1" "; @param (map)(opt) options" ";  {...}" ";" "; @return (map)"])
  ; =>
  ; [{:type :param  :meta ["map" "opt"] :value "options" :additional [" {...}"]}
  ;  {:type :return :meta ["map"]}]
  ;
  ; @return (maps in vector)
  [n]
  (-> n (split-header-blocks)
        (read-header-blocks)))
