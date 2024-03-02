
(ns source-code-documentation.print.engine
    (:require [fruits.vector.api                        :as vector]
              [io.api                                   :as io]
              [source-code-documentation.assemble.cover :as assemble.cover]
              [source-code-documentation.assemble.page  :as assemble.page]
              [source-code-documentation.print.utils    :as print.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-page!
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  [state options file-data]
  (let [page            (assemble.page/assemble-page state options file-data)
        page-print-path (print.utils/page-print-path state options file-data)]
       (io/write-file! page-print-path page {:ensure-eol? true :create? true})))

(defn print-pages!
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  [state options]
  (doseq [file-data state] (print-page! state options file-data)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-cover!
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  [state options]
  (let [cover            (assemble.cover/assemble-cover state options)
        cover-print-path (print.utils/cover-print-path  state options)]
       (io/write-file! cover-print-path cover {:ensure-eol? true :create? true})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-documentation!
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  [state options]
  (let [state (vector/keep-items-by state :create-documentation?)]
       (if (-> options :output-path io/directory-exists?)
           (-> options :output-path io/empty-directory!)
           (-> options :output-path io/create-directory!))
       (print-cover! state options)
       (print-pages! state options)
       ; @DEBUG
       (assemble.page/assemble-page state options (last state))))
       ;(-> state)))
