
(ns source-code-documentation.import.engine
    (:require [fruits.vector.api                      :as vector]
              [io.api                                 :as io]
              [source-code-documentation.import.utils :as import.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-source-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [_ _ {{:keys [defs defns]} :ns-map :keys [filepath] :as file-data}]
  (if-let [file-content (io/read-file filepath {:warn? true})]
          (letfn [(f0 [_ %] [(:name %) (import.utils/def-header  file-content %)])
                  (f1 [_ %] [(:name %) (import.utils/defn-header file-content %)])
                  (f2 [  %] (-> % :value :type (= :symbol)))
                  (f3 [  %] (-> % (assoc-in [:value :symbol] (import.utils/def-value file-content %))))]
                 (-> file-data (update-in [:headers] merge (vector/to-map defs  f0))
                               (update-in [:headers] merge (vector/to-map defns f1))
                               (update-in [:ns-map :defs] vector/update-items-by f2 f3))))) ; <- Reading symbol type values of defs is required (for creating redirection traces).

(defn import-source-files
  ; @ignore
  ;
  ; @description
  ; - Imports headers for defs and defns from all source files within the given source directories.
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle redirections, the documentation generator requires importing headers from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [file-data] (import-source-file state options file-data))]
         (vector/->items state f0)))
