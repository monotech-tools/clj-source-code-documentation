
(ns source-code-documentation.import.engine
    (:require [fruits.vector.api :as vector]
              [io.api :as io]
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
          (letfn [(f0 [%] {:name (:name %) :blocks (import.utils/def-header       file-content %)})
                  (f1 [%] {:name (:name %) :blocks (import.utils/defn-header      file-content %)})
                  (f2 [%] {:name (:name %) :body   (import.utils/def-source-code  file-content %)})
                  (f3 [%] {:name (:name %) :body   (import.utils/defn-source-code file-content %)})
                  (f4 [%] (-> % :value :type (= :symbol)))
                  (f5 [%] (-> % (assoc-in [:value :symbol] (import.utils/def-value file-content %))))]
                 (-> file-data (update-in [:ns-map :defs] vector/update-items-by f4 f5) ; <- Importing symbol type values of defs is required (for creating redirection traces).
                               (update-in [:headers]      vector/concat-items (vector/->items defs  f0))
                               (update-in [:headers]      vector/concat-items (vector/->items defns f1))
                               (update-in [:source-codes] vector/concat-items (vector/->items defs  f2))
                               (update-in [:source-codes] vector/concat-items (vector/->items defns f3))))))

(defn import-source-files
  ; @ignore
  ;
  ; @description
  ; - Imports the headers of defs and defns (from all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle header redirections, the documentation generator requires importing headers from all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (import-source-file state options %))]
         (vector/->items state f0)))
