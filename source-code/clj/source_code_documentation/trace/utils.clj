
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
  ; (def-value-symbol {:ns-map {:defs [{:name "MY-CONSTANT" :value {:symbol "another-namespace/ANOTHER-CONSTANT"} ...} ...] ...} ...}
  ;                   {:name "MY-CONSTANT" :type :def :content [...] ...})
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
  ; (def-value-symbol-namespace {:ns-map {:defs [{:name "MY-CONSTANT" :value {:symbol "another-namespace/ANOTHER-CONSTANT"} ...} ...] ...} ...}
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
  ; (def-value-symbol-name {:ns-map {:defs [{:name "MY-CONSTANT" :value {:symbol "another-namespace/ANOTHER-CONSTANT"} ...} ...] ...} ...}
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
  ; Returns the namespace of the given link pointer / redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (pointer-namespace {...} {...} "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @usage
  ; (pointer-namespace {...} {...} "ANOTHER-CONSTANT")
  ; =>
  ; nil
  ;
  ; @usage
  ; (pointer-namespace {...} {...} "")
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
  ; Returns the name of the given link pointer / redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (pointer-name {...} {...} "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @usage
  ; (pointer-name {...} {...} "ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @usage
  ; (pointer-name {...} {...} "")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/after-last-occurence "/" {:return? true})
              (string/to-nil {:if-empty? true})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn wildcard-target-namespace
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @return (string)
  [file-data section]
  (or (def-value-symbol-namespace file-data section)
      "unknown-wildcard-target"))

(defn wildcard-target-name
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @return (string)
  [file-data section]
  (or (def-value-symbol-name file-data section)
      "unknown-wildcard-target"))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn derive-pointer-namespace
  ; @ignore
  ;
  ; @description
  ; - Derives the namespace from the given link pointer / redirection pointer.
  ; - Uses the file namespace as a fallback value.
  ; - Replaces the '*' wildcard character with the namespace from the declaration's value
  ;   (if the declaration is a def, its value is a symbol and has a namespace).
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (derive-pointer-namespace {...} {...} "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data section pointer]
  (if-let [pointer-namespace (pointer-namespace file-data section pointer)]
          (cond (-> pointer-namespace (not= "*"))
                (-> pointer-namespace)
                :replace-wildcard (wildcard-target-namespace file-data section))
          (-> file-data :ns-map :declaration :name)))

(defn derive-pointer-name
  ; @ignore
  ;
  ; @description
  ; - Derives the name from the given link pointer / redirection pointer.
  ; - Uses the declaration name as a fallback value.
  ; - Replaces the '*' wildcard character with the name from the declaration's value
  ;   (if the declaration is a def and its value is a symbol).
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (string) pointer
  ;
  ; @usage
  ; (derive-pointer-name {...} {...} "another-namespace/ANOTHER-CONSTANT")
  ; =>
  ; "ANOTHER-CONSTANT"
  ;
  ; @return (string)
  [file-data {:keys [name] :as section} pointer]
  (if-let [pointer-name (pointer-name file-data section pointer)]
          (cond (-> pointer-name (not= "*"))
                (-> pointer-name)
                :replace-wildcard (wildcard-target-name file-data section))
          (-> name)))

(defn derive-pointer
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) section
  ; @param (map) snippet
  ;
  ; @usage
  ; (derive-pointer {...} {...} {:marker :redirect :meta ["another-namespace/ANOTHER-CONSTANT"]})
  ; =>
  ; :another-namespace/ANOTHER-CONSTANT
  ;
  ; @return (namespaced keyword)
  [file-data section snippet]
  (let [pointer (-> snippet :meta first)]
       (keyword (-> file-data (derive-pointer-namespace section pointer))
                (-> file-data (derive-pointer-name      section pointer)))))

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
  ; (invoke-pointer-namespace-alias {...} {...} :another-namespace/ANOTHER-CONSTANT)
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
