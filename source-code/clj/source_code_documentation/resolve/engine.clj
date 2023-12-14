
(ns source-code-documentation.resolve.engine
    (:require [source-code-documentation.resolve.utils :as resolve.utils]
              [fruits.vector.api :as vector]
              [io.api :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-pointer
  ; @ignore
  ;
  ; @description
  ; Returns the target declaration that the given pointer points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (namespaced keyword) pointer
  ; @param (string) extension
  ;
  ; @example
  ; (resolve-pointer [...] {...} :another-namespace/another-function "clj")
  ; =>
  ; {:name "another-function" :header [{:type :param :meta ["map"] :value "my-param"}]}
  ;
  ; @return (map)
  [state _ pointer extension]
  (let [redirection-namespace (namespace pointer)
        redirection-name      (name pointer)]
       (letfn [(f0 [%] (-> % :ns-map :declaration :name       (= redirection-namespace))) ; <- Returns TRUE if the given file data corresponds to the target namespace.
               (f1 [%] (-> % :name                            (= redirection-name)))      ; <- Returns TRUE if the given declaration corresponds to the target name.
               (f2 [%] (-> % :filepath io/filepath->extension (= extension)))             ; <- Returns TRUE if the given file data has the same extension as the target has.
               (f3 [%] (and (f0 %) (f2 %)))                                               ; ...
               (f4 [%] (-> % (vector/first-match f3)))                                    ; <- Returns the file data of the target namespace.
               (f5 [%] (-> % (vector/first-match f1)))]                                   ; <- Returns the target declaration.
              (-> state f4 :declarations f5))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-redirection
  ; @ignore
  ;
  ; @description
  ; Returns the target declaration that the given pointer points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (namespaced keyword) pointer
  ;
  ; @example
  ; (resolve-redirection [...] {...} {...} "another-namespace/another-function")
  ; =>
  ; {:name "another-function" :header [{:type :param :meta ["map"] :value "my-param"}]}
  ;
  ; @return (map)
  [state options file-data pointer]
  (let [extension (-> file-data :filepath io/filepath->extension)]
       (if-let [target-declaration (or (resolve-pointer state options pointer extension)
                                       (resolve-pointer state options pointer "cljc"))]
               (-> target-declaration)
               {:header [{:type :error :description :unresolved-pointer-error :pointer pointer}]})))

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
  ; Resolves the redirection (if any) in the header of the given declaration.
  ; If a redirection block is found. it resolves the pointer of it, overwriting the original header blocks with the target header's blocks.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ;
  ; @example
  ; (resolve-redirections [...] {...} {...}
  ;                       {:name   "my-function"
  ;                        :header [{:type :redirect    :meta       ["another-namespace/another-function"]                              :indent 1}
  ;                                 {:type :description :additional ["This is the original description of the 'my-function' function."] :indent 1}]})
  ; =>
  ; {:name   "my-function"
  ;  :header [{:type :description          :additional ["This description is from the header of the 'another-function' function."]             :indent 1}
  ;           {:type :return :meta ["map"] :additional ["This return description is also from the header of the 'another-function' function."] :indent 1}]}
  ;
  ; @return (map)
  [state options file-data declaration]
  (letfn [(f0 [trace %] (or (if-let [% (vector/first-match (:header %) #(= :redirect (:type %)))] (f2 trace %)) %))       ; <- ... takes the original declaration returns the target declaration
          (f1 [trace %] (resolve-redirection state options file-data (:pointer %)))                                       ; <- ... takes a header block returns the target declaration
          (f2 [trace %] (let [trace (resolve.utils/update-trace trace (:pointer %))] (f0 trace (f1 trace %))))] ; <- ... takes a header block returns the target declaration
         (let [initial-trace [(resolve.utils/create-pointer file-data declaration)]]
              (merge declaration (-> initial-trace (f0 declaration))))))

(defn resolve-links
  ; @ignore
  ;
  ; @description
  ; Resolves the links in the header of the given declaration (if any).
  ; If a link block is found, it resolves the pointer of it, inserting the target header's blocks between the original header blocks (where the link was).
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ;
  ; @example
  ; (resolve-links [...] {...} {...}
  ;                {:name   "my-function"
  ;                 :header [{:type :link        :meta       ["another-namespace/another-function"]                              :indent 1}
  ;                          {:type :description :additional ["This is the original description of the 'my-function' function."] :indent 1}]})
  ; =>
  ; {:name   "my-function"
  ;  :header [{:type :description          :additional ["This description is from the header of the 'another-function' function."]             :indent 1}
  ;           {:type :return :meta ["map"] :additional ["This return description is also from the header of the 'another-function' function."] :indent 1}
  ;           {:type :description          :additional ["This is the original description of the 'my-function' function."]                     :indent 1}]}
  ;
  ; @return (map)
  [state options file-data {:keys [header] :as declaration}]
  (letfn [(f0 [trace %] (vector/->items-by % #(= :link (:type %)) #(f2 trace %)))                               ; <- Resolves all link block in the given header.
          (f1 [trace %] (:header (resolve-link state options file-data (:pointer %))))                          ; <- ...
          (f2 [trace %] (let [trace (resolve.utils/update-trace trace (:pointer %))] (f0 trace (f1 trace %))))] ; <- Resolves the given link block and recursivelly resolves the link blocks in the resolved target header also.
         (let [initial-trace [(resolve.utils/create-pointer file-data declaration)]]
              (assoc declaration :header (-> initial-trace (f0 header) vector/flat-items)))))

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
         (-> file-data (update :declarations vector/->items f0)    ; <- Resolves redirections at first,
                       (update :declarations vector/->items f1)))) ; <- then resolves links.

(defn resolve-imported-files
  ; @ignore
  ;
  ; @description
  ; - Resolves the traced links and redirections of defs and defns resolving header link and redirection pointers (for all source files within the given source directories).
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
