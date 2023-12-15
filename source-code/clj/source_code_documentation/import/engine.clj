
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
          (letfn [(f0 [%] {:name (:name %) :header (import.utils/def-header  file-content %)
                                           :body   (import.utils/def-body    file-content %)})
                  (f1 [%] {:name (:name %) :header (import.utils/defn-header file-content %)
                                           :body   (import.utils/defn-body   file-content %)})
                  (f4 [%] (-> % :value :type (= :symbol)))
                  (f5 [%] (-> % (assoc-in [:value :symbol] (import.utils/def-value file-content %))))]
                 (-> file-data (update-in [:ns-map :defs] vector/update-items-by f4 f5) ; <- Importing symbol type values of defs (for creating redirection traces).
                               (update-in [:declarations] vector/concat-items (vector/->items defs  f0))
                               (update-in [:declarations] vector/concat-items (vector/->items defns f1))))))

(defn import-source-files
  ; @ignore
  ;
  ; @description
  ; - Imports the source codes and headers of defs and defns (from all mapped source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle links and redirections, it requires importing source codes and headers from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (import-source-file state options %))]
         (vector/->items state f0)))
