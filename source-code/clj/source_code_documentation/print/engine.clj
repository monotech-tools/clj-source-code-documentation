
(ns source-code-documentation.print.engine
    (:require
              [io.api :as io]
              [fruits.vector.api :as vector]
              [source-code-documentation.print.assemble :as print.assemble]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-page!
  [state options file-data]
  (let [page (print.assemble/assemble-page state options file-data)]))
       ;(io/write-file! (str (:output-path options) "/test.html") page)))


(defn print-pages!
  [])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-documentation!
  [state options]
  (let [state (vector/keep-items-by state :create-documentation?)]
       (when-let [file-data (second state)]
                 (io/write-file! (str (:output-path options) "/test2.html") (print.assemble/assemble-page state options file-data)
                                 {:create? true}))
       (print.assemble/assemble-page state options (second state))))
