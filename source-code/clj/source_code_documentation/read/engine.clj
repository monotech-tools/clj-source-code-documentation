
(ns source-code-documentation.read.engine
    (:require [fruits.vector.api                    :as vector]
              [source-code-documentation.read.utils :as read.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [_ _ file-data]
  (-> file-data (update :sections vector/->items read.utils/read-section-content)
                (update :sections vector/->items read.utils/read-section-source-code)))

(defn read-imported-files
  ; @ignore
  ;
  ; @description
  ; - Reads the imported source codes and documentation contents of defs and defns (from all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle links and redirections, it requires reading source codes and documentation contents from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (read-imported-file state options %))]
         (vector/->items state f0)))
