
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
          (letfn [(f0 [%] {:name (:name %) :content     (import.utils/import-def-content      file-content %)
                                           :source-code (import.utils/import-def-source-code  file-content %)
                                           :type :def})
                  (f1 [%] {:name (:name %) :content     (import.utils/import-defn-content     file-content %)
                                           :source-code (import.utils/import-defn-source-code file-content %)
                                           :type :defn})
                  (f2 [%] (-> % :value :type (= :symbol)))
                  (f3 [%] (-> % (assoc-in [:value :symbol] (import.utils/import-def-value file-content %))))]
                 (-> file-data (update-in [:ns-map :defs] vector/update-items-by f2 f3) ; <- Importing symbol type values of defs (for creating redirection traces).
                               (update-in [:sections] vector/concat-items (vector/->items defs  f0))
                               (update-in [:sections] vector/concat-items (vector/->items defns f1))
                               (update-in [:sections] vector/concat-items (import.utils/import-tutorials file-content))))))

(defn import-source-files
  ; @ignore
  ;
  ; @description
  ; - Imports the source codes and documentation contents of defs and defns (from all mapped source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle links and redirections, it requires importing source codes and documentation contents from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (import-source-file state options %))]
         (vector/->items state f0)))
