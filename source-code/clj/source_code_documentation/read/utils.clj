
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-header
  ;
  ; @ignore
  ;
  ; @param (strings in vector) n
  ;
  ; @return (map)
  [n]
  (letfn [(f0 [%] (regex/re-first % #"^(?<=\;[\s\t]{0,}\@)[a-z]{1,}"))]))
