
(ns source-code-documentation.resolve.engine
    (:require [source-code-documentation.resolve.utils :as resolve.utils]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-header-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (maps in vector)
  [state _ _ _ {:keys [pointer]}]
  (let [redirection-namespace (namespace pointer) redirection-name (name pointer)]
       (letfn [(f0 [%] (-> % :ns-map :declaration :name (= redirection-namespace)))
               (f1 [%] (-> % :name                      (= redirection-name)))
               (f2 [%] (-> % (vector/first-match f0)))
               (f3 [%] (-> % (vector/first-match f1)))]
              (if-let [target-header (-> state f2 :headers f3)]
                      (-> target-header :blocks)
                      [{:type :error :description :unresolved-redirection-error :pointer pointer}]))))

(defn resolve-header-redirections
  ; @ignore
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
  ;  :blocks [{:type :description :additional ["This description is from the redirected header of the 'another-function' function."] :indent 1}
  ;           {:type :description :additional ["This is the original description of the 'my-function' function."]                    :indent 1}]}
  ;
  ; @return (maps in vector)
  [state options file-data {:keys [blocks name] :as header}]


  (letfn [(f0 [      %] (-> % :type (= :redirect)))
          (f1 [      %] (-> file-data :ns-map :declaration :name) (keyword %))
          (f2 [trace %] (-> % (vector/->items-by f0 #(f3 trace %))))
          (f3 [trace %] (if (vector/contains-item? trace (:pointer %))
                            (let [trace (conj trace (:pointer %))]
                                 (throw (Exception. (str "Circular redirection error.\n" trace))))
                            (let [trace (conj trace (:pointer %))]
                                 (f2 trace (resolve-header-redirection state options file-data header %)))))]
         (let [initial-trace [(f1 name)]]
              (assoc header :blocks (-> initial-trace (f2 blocks) vector/flat-items)))))

(defn resolve-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options file-data]
  (letfn [(f0 [%] (resolve-header-redirections state options file-data %))]
         (update file-data :headers vector/->items f0)))

(defn resolve-imported-files
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (resolve-imported-file state options %))]
         (vector/->items state f0)))
