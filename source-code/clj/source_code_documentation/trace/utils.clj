
(ns source-code-documentation.trace.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn def-value-symbol
  ; @ignore
  ;
  ; @description
  ; Returns the value of a specific def declaration in case its type is symbol (symbol type def declaration values are imported).
  ;
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @usage
  ; (def-value-symbol {:ns-map {:defs [{:name "MY-CONSTANT" :value {:symbol "another-namespace/ANOTHER-CONSTANT"}}]}}
  ;                   {:name "MY-CONSTANT" :type :def :content [...]})
  ; =>
  ; "another-namespace/ANOTHER-CONSTANT"
  ;
  ; @return (string)
  [file-data {:keys [name]}]
  (letfn [(f0 [%] (-> % :name (= name)))]
         (-> file-data :ns-map :defs (vector/first-match f0) :value :symbol)))

(defn def-value-symbol-namespace
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @usage
  ; (def-value-symbol-namespace {:ns-map {:defs [{:name "MY-CONSTANT" :value {:symbol "another-namespace/ANOTHER-CONSTANT"}}]}}
  ;                             {:name "MY-CONSTANT" :type :def :content [...]})
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data section]
  (-> file-data (def-value-symbol section)
                (string/before-first-occurence "/" {:return? false})
                (string/to-nil {:if-empty? true})))

(defn def-value-symbol-name
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @usage
  ; (def-value-symbol-name {:ns-map {:defs [{:name "MY-CONSTANT" :value {:symbol "another-namespace/ANOTHER-CONSTANT"}}]}}
  ;                        {:name "MY-CONSTANT" :type :def :content [...]})
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @return (string)
  [file-data section]
  (-> file-data (def-value-symbol section)
                (string/after-last-occurence "/" {:return? true})
                (string/to-nil {:if-empty? true})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn pointer-namespace
  ; @ignore
  ;
  ; @description
  ; Returns the namespace of the given link / redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (pointer-namespace {...}
  ;                    {:name "MY-CONSTANT" :type :def :content [...]}
  ;                    "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @usage
  ; (pointer-namespace {...}
  ;                    {:name "MY-CONSTANT" :content [...]}
  ;                    "ANOTHER-CONSTANT")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/before-first-occurence "/" {:return? false})
              (string/to-nil {:if-empty? true})))

(defn pointer-name
  ; @ignore
  ;
  ; @description
  ; Returns the name of the given link / redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (pointer-name {...}
  ;               {:name "MY-CONSTANT" :type :def :content [...]}
  ;               "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @usage
  ; (pointer-name {...}
  ;               {:name "MY-CONSTANT" :type :def :content [...]}
  ;               "ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @usage
  ; (pointer-name {...}
  ;               {:name "MY-CONSTANT" :type :def :content [...]}
  ;               "")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/after-last-occurence "/" {:return? true})
              (string/to-nil {:if-empty? true})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn derive-pointer-namespace
  ; @ignore
  ;
  ; @description
  ; - Derives the namespace from the given link / redirection pointer.
  ; - Uses the namespace from the declaration's value (if any, symbol type, and the declaration is a def) as a fallback.
  ; - Uses the file namespace as a second fallback.
  ; - Replaces the '*' wildcard character with the file namespace.
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (derive-pointer-namespace {...}
  ;                           {:name "MY-CONSTANT" :type :def :content [...]}
  ;                           "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data section pointer]
  (let [file-namespace (-> file-data :ns-map :declaration :name)]
       (-> (or (pointer-namespace          file-data section pointer)
               (def-value-symbol-namespace file-data section)
               (-> file-namespace))
           (regex/replace-match #"^\*$" file-namespace))))

(defn derive-pointer-name
  ; @ignore
  ;
  ; @description
  ; - Derives the name from the given link / redirection pointer.
  ; - Uses the name from the declaration's value (if any, symbol type, and the declaration is a def) as a fallback.
  ; - Uses the declaration name as a second fallback.
  ; - Replaces the '*' wildcard character with the declaration name.
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (derive-pointer-name {...}
  ;                      {:name "MY-CONSTANT" :type :def :content [...]}
  ;                      "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @return (string)
  [file-data {:keys [name] :as section} pointer]
  (-> (or (pointer-name          file-data section pointer)
          (def-value-symbol-name file-data section)
          (-> name))
      (regex/replace-match #"^\*$" name)))

(defn derive-pointer
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (map) content-block
  ;
  ; @usage
  ; (derive-pointer {...}
  ;                 {:name "MY-CONSTANT" :type :def :content [...]}
  ;                 {:type :redirect :meta "..."})
  ; =>
  ; :another-namespace/ANOTHER-CONSTANT
  ;
  ; @return (namespaced keyword)
  [file-data section content-block]
  (let [pointer (-> content-block :meta first)]
       (keyword (derive-pointer-namespace file-data section pointer)
                (derive-pointer-name      file-data section pointer))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn invoke-pointer-namespace-alias
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (namespaced pointer) pointer
  ;
  ; @usage
  ; (invoke-pointer-namespace-alias {...}
  ;                                 {:name "MY-CONSTANT" :type :def :content [...]}
  ;                                 :another-namespace/ANOTHER-CONSTANT)
  ;
  ; @return (namespaced keyword)
  [file-data _ pointer]
  (let [pointer-namespace (namespace pointer)
        pointer-name      (name      pointer)]
       (letfn [(f0 [%] (if (-> % :alias (= pointer-namespace))
                           (-> % :name)))]
              (if-let [pointer-namespace (->> file-data :ns-map :declaration :require :deps (some f0))]
                      (keyword pointer-namespace pointer-name)
                      (-> pointer)))))
