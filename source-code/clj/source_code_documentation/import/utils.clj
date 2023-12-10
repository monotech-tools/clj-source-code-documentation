
(ns source-code-documentation.import.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn last-coherent-comment-row-group
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @example
  ; (last-coherent-comment-row-group "\n; Row #1\n(let []); Row #2\n; Row #3")
  ; =>
  ; ["; Row #2"
  ;  "; Row #3"]
  ;
  ; @return (strings in vector)
  [header]
  (letfn [(f0 [       %] (regex/re-match? % #"^[\s\t]{0,}\;"))  ; <- Returns TRUE if the given value is a comment row.
          (f1 [       %] (regex/re-match? % #"\n[\s\t]{0,}\;")) ; <- Returns TRUE if the given value contains any comment rows.
          (f2 [result %] (conj result (string/trim %)))]        ; <- Trims the given value (comment row) then appends it to the result vector.
         (loop [observed-part header result []]
               (let [row-ends-at (or (string/first-dex-of observed-part "\n") (count observed-part))
                     row-content (string/keep-range observed-part 0 (->  row-ends-at))
                     rest-part   (string/keep-range observed-part   (inc row-ends-at))]
                    (cond (-> observed-part string/empty?) (-> result)
                          (-> row-content f0)              (-> rest-part (recur (f2 result row-content)))
                          (-> rest-part   f1)              (-> rest-part (recur []))
                          :no-further-comment-rows         (-> rest-part (recur result)))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn def-value
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @return (string)
  [file-content {:keys [value]}]
  (string/keep-range file-content (-> value :bounds first)
                                  (-> value :bounds second)))

(defn def-header
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @return (strings in vector)
  [file-content {:keys [bounds]}]
  (-> file-content (string/keep-range 0 (first bounds))                        ; <- Cuts the rest of the file content from the start position of the def.
                   (string/trim-end)                                           ; <- Removes the indent (if any) that precedes the def.
                   (regex/after-last-match #"\n[\s\t]{0,}\n" {:return? false}) ; <- Keeps the part after the last empty row.
                   (last-coherent-comment-row-group)))                         ; <- Extracts the last coherent comment row group.

(defn defn-header
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (map) defn
  ;
  ; @return (strings in vector)
  [file-content {:keys [bounds]}]
  (-> file-content (string/keep-range (first bounds) (last bounds))              ; <- Keeps the part of the file content from the start position of the defn - to its end position.
                   (regex/before-first-match #"\n[\s\t]{1,}\[|\n[\s\t]{1,}\(\[") ; <- Cuts the part from the first (non-commented and non-quoted) argument list.
                   (last-coherent-comment-row-group)))                           ; <- Extracts the last coherent comment row group.
