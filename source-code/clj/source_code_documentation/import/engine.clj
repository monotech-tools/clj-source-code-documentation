
(ns source-code-documentation.import.engine
    (:require [fruits.vector.api                      :as vector]
              [io.api                                 :as io]
              [source-code-documentation.import.utils :as import.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-source-file
  ; @ignore
  ;
  ; @description
  ; - Imports def and defn headers as sections.
  ; - Imports tutorials as sections.
  ; - Imports def and defn source codes.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; {:filepath (string)
  ;  :ns-map (map)
  ;   {:defs (maps in vector)(opt)
  ;    :defns (maps in vector)(opt)
  ;    ...}
  ;  ...}
  ;
  ; @usage
  ; (import-source-file [...] {...} {...})
  ; =>
  ; {:create-documentation? true
  ;  :filepath              "source-code/my_namespace_a.clj"
  ;  :ns-map                {...}
  ;  :sections [{:name "MY-CONSTANT" :label "MY-CONSTANT" :content ["Row #1" "Row #2" ...] :type :def  :source-code "..."}
  ;             {:name "my-function" :label "my-function" :content ["Row #1" "Row #2" ...] :type :defn :source-code "..."}
  ;             {:name "my-tutorial" :label "My tutorial" :content ["Row #1" "Row #2" ...] :type :tutorial}
  ;             ...]}
  ;
  ; @return (map)
  [_ _ {:keys [filepath] :as file-data}]
  (if-let [file-content (io/read-file filepath {:warn? true})]
          (-> file-data (assoc-in  [:ns-map :defs]                 (import.utils/import-def-values file-data file-content))
                        (update-in [:sections] vector/concat-items (import.utils/import-defs       file-data file-content))
                        (update-in [:sections] vector/concat-items (import.utils/import-defns      file-data file-content))
                        (update-in [:sections] vector/concat-items (import.utils/import-tutorials  file-data file-content)))))

(defn import-source-files
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @usage
  ; (import-source-files [{:filepath "source-code/my_namespace_a.clj" :create-documentation? true  :ns-map {...}}
  ;                       {:filepath "source-code/my_namespace_b.clj" :create-documentation? false :ns-map {...}}
  ;                       {:filepath "source-code/my_namespace_c.clj" :create-documentation? false :ns-map {...}}]
  ;                     {:filename-pattern #"my\_namespace\_a\.clj" :source-paths ["source-code"]})
  ; =>
  ; [{:filepath "source-code/my_namespace_a.clj" :create-documentation? true  :ns-map {...} :sections [{...} ...]}
  ;  {:filepath "source-code/my_namespace_b.clj" :create-documentation? false :ns-map {...} :sections [{...} ...]}
  ;  {:filepath "source-code/my_namespace_c.clj" :create-documentation? false :ns-map {...} :sections [{...} ...]}]
  ;
  ; @return (maps in vector)
  ; [(map) file-data
  ;   {:create-documentation? (boolean)
  ;    :filepath (string)
  ;    :ns-map (map)
  ;    :sections (maps in vector)}]
  [state options]
  (letfn [(f0 [%] (import-source-file state options %))]
         (vector/->items state f0)))
