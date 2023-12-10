
(ns source-code-documentation.read.engine
    (:require [fruits.vector.api :as vector]
              [fruits.map.api :as map]
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
  (update file-data :headers map/->values read.utils/read-header))

(defn read-imported-files
  ; @ignore
  ;
  ; @description
  ; - Reads the imported headers of defs and defns (from all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle header redirections, the documentation generator requires reading headers from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [file-data] (read-imported-file state options file-data))]
         (vector/->items state f0)))
