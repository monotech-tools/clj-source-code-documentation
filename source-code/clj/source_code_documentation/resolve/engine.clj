
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
  ; Returns the target header that the given pointer points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (namespaced keyword) pointer
  ; @param (string) extension
  ;
  ; @example
  ; (resolve-pointer [...] {...} :another-namespace/another-function "clj")
  ; =>
  ; {:name "another-function" :blocks [{:type :param :meta ["map"] :value "my-param"}]}
  ;
  ; @return (map)
  [state _ pointer extension]
  (let [redirection-namespace (namespace pointer)
        redirection-name      (name pointer)]
       (letfn [(f0 [%] (-> % :ns-map :declaration :name       (= redirection-namespace))) ; <- Returns TRUE if the given 'file-data' corresponds to the target namespace.
               (f1 [%] (-> % :name                            (= redirection-name)))      ; <- Returns TRUE if the given 'header' corresponds to the target name.
               (f2 [%] (-> % :filepath io/filepath->extension (= extension)))             ; <- Returns TRUE if the given 'file-data' has the same extension as the target has.
               (f3 [%] (and (f0 %) (f2 %)))                                               ; ...
               (f4 [%] (-> % (vector/first-match f3)))                                    ; <- Returns the 'file-data' of the target namespace.
               (f5 [%] (-> % (vector/first-match f1)))]                                   ; <- Returns the target header.
              (-> state f4 :headers f5))))






(defn redirection-pointer
  [_ _ header]
  (letfn [(f0 [%] (= :redirect (:type %)))
          (f1 [%] (vector/first-match % f1))]
         (-> header :blocks f1 :pointer)))

(defn resolve-redirection
  [state options pointer extension]
  (or (resolve-pointer state options pointer extension)
      (resolve-pointer state options pointer "cljc")
      {:blocks [{:type :error :description :unresolved-pointer-error :pointer pointer}]}))

(defn resolve-trace
  [state options trace extension]
  (letfn [(f0 [%] (resolve-redirection state options % extension))
          (f1 [%] (redirection-pointer state options %))
          (f2 [%] (resolve-trace       state options % extension))
          (f3 [%] (resolve.utils/update-trace trace %))]
         (if-let [next-pointer (-> trace last f0 :blocks f1)]
                 (-> next-pointer f3 f2)
                 (-> trace))))

; [:ns1/f1 :ns2/f2]
;

(defn resolve-header-link
  ; @ignore
  ;
  ; @description
  ; Returns the blocks of the target header whose the given pointer points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (maps in vector)
  [state options file-data _ {:keys [pointer trace] :as header-block}]
  ; Resolving header links is the same process as resolving header redirections.
  ;(resolve-header-redirection state options file-data pointer))

  (let [extension (-> file-data :filepath io/filepath->extension)]
       (if-let [target-header (or (resolve-pointer state options pointer extension)
                                  (resolve-pointer state options pointer "cljc"))]
               ;(-> target-header)
               (-> target-header :blocks); (vector/->items #(assoc % :trace trace)))
               [{:type :error :description :unresolved-pointer-error :pointer pointer}])))


(defn resolve-header-links_
  [state options file-data header]
  (let [initial-trace [(resolve.utils/create-pointer file-data header)]]
       (letfn [(f0 [%] (-> % :type (= :link)))
               (f1 [%] (-> % (assoc :trace initial-trace)))
               (f2 [%] (resolve-header-link state options file-data header (f1 %)))]
              ()
              (update header :blocks vector/->items-by f0 f2))))

; {:blocks [{:type :link :pointer :ns2/f2}
;           {:type :description :additional [...]}]}

; {:blocks [[{:type :link :pointer :ns3/f3}]
;           {:type :description :additional [...]}]}










;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-header-redirection
  ; @ignore
  ;
  ; @description
  ; Returns the target header that the pointer in the given header block points to.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (namespaced keyword) pointer
  ;
  ; @example
  ; (resolve-header-redirection [...] {...} {...} "another-namespace/another-function")
  ; =>
  ; {:name "another-function" :blocks [{:type :param :meta ["map"] :value "my-param"}]}
  ;
  ; @return (map)
  [state options file-data pointer]
  (let [extension (-> file-data :filepath io/filepath->extension)]
       (if-let [target-header (or (resolve-pointer state options pointer extension)
                                  (resolve-pointer state options pointer "cljc"))]
               (-> target-header)
               {:blocks [{:type :error :description :unresolved-pointer-error :pointer pointer}]})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-source-code-redirections
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) source-code
  ;
  ; @return (map)
  [state options file-data {:keys [blocks] :as header}])

(defn resolve-header-redirections
  ; @ignore
  ;
  ; @description
  ; Resolves the redirection (if any) in the given header.
  ; If a redirection block is found it resolves the pointer of it, overwriting the original header blocks with the target header's blocks.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (resolve-header-redirections [...] {...} {...}
  ;                              {:name   "my-function"
  ;                               :blocks [{:type :redirect    :meta       ["another-namespace/another-function"]                              :indent 1}
  ;                                        {:type :description :additional ["This is the original description of the 'my-function' function."] :indent 1}]})
  ; =>
  ; {:name   "my-function"
  ;  :blocks [{:type :description          :additional ["This description is from the header of the 'another-function' function."]        :indent 1}
  ;           {:type :return :meta ["map"] :additional ["This return description is from the header of the 'another-function' function."] :indent 1}]}
  ;
  ; @return (map)
  [state options file-data {:keys [blocks] :as header}]
  (letfn [(f0 [trace %] (or (if-let [% (vector/first-match % #(= :redirect (:type %)))] (f2 trace %)) %))       ; <- ...
          (f1 [trace %] (:blocks (resolve-header-redirection state options file-data (:pointer %))))            ; <- ...
          (f2 [trace %] (let [trace (resolve.utils/update-trace trace (:pointer %))] (f0 trace (f1 trace %))))] ; <- ...
         (let [initial-trace [(resolve.utils/create-pointer file-data header)]]
              (assoc header :blocks (-> initial-trace (f0 blocks))))))

(defn resolve-header-links
  ; @ignore
  ;
  ; @description
  ; Resolves the links in the given header (if any).
  ; If a link block is found it resolves the pointer of it, inserting the target header's blocks between the original header blocks (where the link was).
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (resolve-header-links [...] {...} {...}
  ;                       {:name   "my-function"
  ;                        :blocks [{:type :link        :meta       ["another-namespace/another-function"]                              :indent 1}
  ;                                 {:type :description :additional ["This is the original description of the 'my-function' function."] :indent 1}]})
  ; =>
  ; {:name   "my-function"
  ;  :blocks [{:type :description          :additional ["This description is from the header of the 'another-function' function."]        :indent 1}
  ;           {:type :return :meta ["map"] :additional ["This return description is from the header of the 'another-function' function."] :indent 1}
  ;           {:type :description          :additional ["This is the original description of the 'my-function' function."]                :indent 1}]}
  ;
  ; @return (map)
  [state options file-data {:keys [blocks] :as header}]
  (letfn [(f0 [trace %] (vector/->items-by % #(= :link (:type %)) #(f2 trace %)))                               ; <- Resolves all link block in the given header blocks.
          (f1 [trace %] (:blocks (resolve-header-redirection state options file-data (:pointer %))))            ; <- ...
          (f2 [trace %] (let [trace (resolve.utils/update-trace trace (:pointer %))] (f0 trace (f1 trace %))))] ; <- Resolves the given link block and recursivelly resolves the link blocks in the resolved target header also.
         (let [initial-trace [(resolve.utils/create-pointer file-data header)]]
              (assoc header :blocks (-> initial-trace (f0 blocks) vector/flat-items)))))

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
  (letfn [(f0 [%] (resolve-header-redirections      state options file-data %))
          (f1 [%] (resolve-source-code-redirections state options file-data %))
          (f2 [%] (resolve-header-links             state options file-data %))]
         (-> file-data (update :headers      vector/->items f0))))    ; <- Resolves the redirections at first,
                       ;(update :headers      vector/->items f2))))    ; <- then resolves the links.
                       ;(update :source-codes vector/->items f1)))) ; <- Additionally, redirects the source codes of def and defn declarations whose headers have been redirected.

(defn resolve-imported-files
  ; @ignore
  ;
  ; @description
  ; - Resolves the traced headers of defs and defns resolving header link and redirection pointers (for all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle header links and redirections, it requires resolving headers for all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (resolve-imported-file state options %))]
         (vector/->items state f0)))
