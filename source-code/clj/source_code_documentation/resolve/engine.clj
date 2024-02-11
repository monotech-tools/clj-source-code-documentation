
(ns source-code-documentation.resolve.engine
    (:require [fruits.vector.api                       :as vector]
              [io.api                                  :as io]
              [source-code-documentation.resolve.utils :as resolve.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-pointer
  ; @ignore
  ;
  ; @description
  ; Returns the target section that the given pointer points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (namespaced keyword) pointer
  ; @param (string) extension
  ;
  ; @usage
  ; (resolve-pointer [...] {...} :another-namespace/another-function "clj")
  ; =>
  ; {:name "another-function" :content [{:type :param :meta ["map"] :name "my-param"}]}
  ;
  ; @return (map)
  [state _ pointer extension]
  (let [redirection-namespace (namespace pointer)
        redirection-name      (name pointer)]
       (letfn [(f0 [%] (-> % :ns-map :declaration :name       (= redirection-namespace))) ; <- Returns TRUE if the given file data corresponds to the target namespace.
               (f1 [%] (-> % :name                            (= redirection-name)))      ; <- Returns TRUE if the given section corresponds to the target name.
               (f2 [%] (-> % :filepath io/filepath->extension (= extension)))             ; <- Returns TRUE if the given file data has the same extension as the target has.
               (f3 [%] (and (f0 %) (f2 %)))                                               ; ...
               (f4 [%] (-> % (vector/first-match f3)))                                    ; <- Returns the file data of the target namespace.
               (f5 [%] (-> % (vector/first-match f1)))]                                   ; <- Returns the target section.
              (-> state f4 :sections f5))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-redirection
  ; @ignore
  ;
  ; @description
  ; Returns the target section that the given pointer points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (namespaced keyword) pointer
  ;
  ; @usage
  ; (resolve-redirection [...] {...} {...} "another-namespace/another-function")
  ; =>
  ; {:name "another-function" :content [{:type :param :meta ["map"] :name "my-param"}]}
  ;
  ; @return (map)
  [state options file-data pointer]
  (let [extension (-> file-data :filepath io/filepath->extension)]
       (if-let [target-section (or (resolve-pointer state options pointer extension)
                                   (resolve-pointer state options pointer "cljc"))]
               (-> target-section)
               {:content [{:type :error :description :unresolved-pointer-error :pointer pointer}]})))

(defn resolve-link
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (namespaced keyword) pointer
  ;
  ; @return (map)
  [state options file-data pointer]
  ; Resolving links is the same process as resolving redirections.
  (resolve-redirection state options file-data pointer))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-redirections
  ; @ignore
  ;
  ; @description
  ; Resolves the redirection (if any) in the documentation content of the given section.
  ; If a redirection block is found. it resolves the pointer of it, overwriting the original content blocks with the target content's blocks.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @usage
  ; (resolve-redirections [...] {...} {...}
  ;                       {:name    "my-function"
  ;                        :type    :defn
  ;                        :content [{:type :redirect    :meta ["another-namespace/another-function"]                              :indent 1}
  ;                                  {:type :description :text ["This is the original description of the 'my-function' function."] :indent 1}]})
  ; =>
  ; {:label   "my-function"
  ;  :name    "my-function"
  ;  :type    :defn
  ;  :content [{:type :description          :text ["This description is from the documentation of the 'another-function' function."]             :indent 1}
  ;            {:type :return :meta ["map"] :text ["This return description is also from the documentation of the 'another-function' function."] :indent 1}]}
  ;
  ; @return (map)
  [state options file-data section]
  (letfn [(f0 [trace %] (or (if-let [% (vector/first-match (:content %) #(= :redirect (:type %)))] (f2 trace %)) %)) ; <- ... takes the original section, returns the target section
          (f1 [trace %] (resolve-redirection state options file-data (:pointer %)))                                  ; <- ... takes a content block, returns the target section
          (f2 [trace %] (let [trace (resolve.utils/update-trace trace (:pointer %))] (f0 trace (f1 trace %))))]      ; <- ... takes a content block, returns the target section
         (let [initial-trace [(resolve.utils/create-pointer file-data section)]]
              (-> section (merge (-> initial-trace (f0 section)))
                          (assoc :name (:name section)))))) ; <- The name of the section is not redirected.

(defn resolve-links
  ; @ignore
  ;
  ; @description
  ; Resolves the links (if any) in the documentation content of the given section.
  ; If a link block is found, it resolves the pointer of it, inserting the target content's blocks between the original content blocks (where the link was).
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @usage
  ; (resolve-links [...] {...} {...}
  ;                {:name    "my-function"
  ;                 :type    :defn
  ;                 :content [{:type :link        :meta ["another-namespace/another-function"]                              :indent 1}
  ;                           {:type :description :text ["This is the original description of the 'my-function' function."] :indent 1}]})
  ; =>
  ; {:label   "my-function"
  ;  :name    "my-function"
  ;  :type    :defn
  ;  :content [{:type :description          :text ["This description is from the documentation of the 'another-function' function."]             :indent 1}
  ;            {:type :return :meta ["map"] :text ["This return description is also from the documentation of the 'another-function' function."] :indent 1}
  ;            {:type :description          :text ["This is the original description of the 'my-function' function."]                            :indent 1}]}
  ;
  ; @return (map)
  [state options file-data {:keys [content] :as section}]
  (letfn [(f0 [trace %] (vector/->items-by % #(= :link (:type %)) #(f2 trace %)))                               ; <- Resolves all link blocks in the given documentation content.
          (f1 [trace %] (:content (resolve-link state options file-data (:pointer %))))                         ; <- ...
          (f2 [trace %] (let [trace (resolve.utils/update-trace trace (:pointer %))] (f0 trace (f1 trace %))))] ; <- Resolves the given link block and recursivelly resolves link blocks in the resolved target content also.
         (let [initial-trace [(resolve.utils/create-pointer file-data section)]]
              (assoc section :content (-> initial-trace (f0 content) vector/flat-items)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options file-data]
  (letfn [(f0 [%] (resolve-redirections state options file-data %))
          (f1 [%] (resolve-links        state options file-data %))]
         (-> file-data (update :sections vector/->items f0)    ; <- Resolves redirections at first,
                       (update :sections vector/->items f1)))) ; <- then resolves links.

(defn resolve-imported-files
  ; @ignore
  ;
  ; @description
  ; - Resolves the traced links and redirections of defs and defns resolving documentation content link and redirection pointers (for all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle links and redirections, it requires resolving them for all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (resolve-imported-file state options %))]
         (vector/->items state f0)))
