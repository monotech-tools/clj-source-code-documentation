
(ns source-code-documentation.read.engine
    (:require [fruits.vector.api :as vector]
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
  (-> file-data (update :declarations vector/->items read.utils/read-declaration-header)
                (update :declarations vector/->items read.utils/read-declaration-body)))

(defn read-imported-files
  ; @ignore
  ;
  ; @description
  ; - Reads the imported source codes and headers of defs and defns (from all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle links and redirections, it requires reading source codes and headers from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (read-imported-file state options %))]
         (vector/->items state f0)))
