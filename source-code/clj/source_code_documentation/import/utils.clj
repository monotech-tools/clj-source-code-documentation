
(ns source-code-documentation.import.utils
    (:require [string.api :as string]
              [regex.api :as regex]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn last-coherent-comment-rows
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @return (strings in vector)
  [n]
  (letfn [(f0 [result row-content] (conj result (string/trim row-content)))]
         (loop [observed-part n result []]
               (let [row-ends-at (or (string/first-dex-of observed-part "\n") (count observed-part))
                     row-content (string/keep-range observed-part 0 (->  row-ends-at))
                     rest-part   (string/keep-range observed-part   (inc row-ends-at))]
                    (cond (string/empty?   observed-part)                                     (-> result)
                          (regex/re-match? row-content #"^[\s\t]{0,}\;")     (recur rest-part (-> result (f0 row-content)))
                          (regex/re-match? rest-part   #"\n[\s\t]{0,}\;")    (recur rest-part (-> []))
                          :rest-part-does-not-contain-further-commented-rows (recur rest-part (-> result)))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn def-header
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @return (strings in vector)
  [file-content {:keys [bounds]}]
  (-> file-content (string/keep-range 0 (first bounds))
                   (string/after-last-occurence "\n\n" {:return? false})
                   (string/trim-end) ; <- Removes indent (if any) before the def.
                   (last-coherent-comment-rows)))

(defn defn-header
      ; @ignore
      ;
      ; @param (string) file-content
      ; @param (map) defn
      ;
      ; @return (strings in vector)
      [file-content {:keys [bounds]}]
      (-> file-content (string/keep-range (first bounds) (last bounds))
                       (regex/before-first-match #"(?<=\n[\s\t]{1,})\[|(?<=\n[\s\t]{1,})\(") ; <- Before first non-commented and non-quoted argument list.
                       (string/trim-end)                                                     ; <- Removes indent (if any) before the argument list.
                       (last-coherent-comment-rows)))
