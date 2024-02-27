
(ns source-code-documentation.assemble.utils
    (:require [fruits.hiccup.api :as hiccup]
              [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [fruits.vector.api :as vector]
              [io.api            :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn normalize-marker
  ; @ignore
  ;
  ; @description
  ; Normalizes internal markers (e.g., :*source-code*).
  ;
  ; @param (keyword) n
  ;
  ; @usage
  ; (normalize-marker :*source-code*)
  ; =>
  ; :source-code
  ;
  ; @return (keyword)
  [n]
  (if (and (-> n name string/first-character (= "*"))
           (-> n name string/last-character  (= "*")))
      (-> n name string/remove-first-character string/remove-last-character keyword)
      (-> n)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn color-class
  ; @ignore
  ;
  ; @param (keyword) n
  ;
  ; @usage
  ; (color-class (:marker-color {...}))
  ; =>
  ; :color--hard-grey
  ;
  ; @return (keyword)
  [n]
  (case n :default   :color--hard-grey
          :primary   :color--hard-blue
          :secondary :color--hard-purple
          :muted     :color--soft-grey
          :success   :color--hard-green
          :warning   :color--hard-red
                     nil))

(defn size-class
  ; @ignore
  ;
  ; @param (keyword) n
  ;
  ; @usage
  ; (size-class (:marker-size {...}))
  ; =>
  ; :text--s
  ;
  ; @return (keyword)
  [n]
  (case n :xxs :text--xxs
          :xs  :text--xs
          :s   :text--s
          :m   :text--m
          :l   :text--l
          :xl  :text--xl
          :xxl :text--xxl
               nil))

(defn visibility-class
  ; @ignore
  ;
  ; @param (boolean) n
  ;
  ; @usage
  ; (visibility-class (:hide-marker? {...}))
  ; =>
  ; :text-hidden
  ;
  ; @return (keyword)
  [n]
  (if n :text--hidden))

(defn config-snippet
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; {:snippet-config (map)(opt)
  ;  ...}
  ; @param (map) snippet
  ; {:marker (keyword)
  ;  :text (strings in vector)(opt)
  ;  ...}
  ;
  ; @return (map)
  [_ {:keys [snippet-config]} {:keys [marker text] :as x}]
  {:collapsed?   (and (-> snippet-config marker :collapsed?))
   :collapsible? (and (-> snippet-config marker :collapsible?)
                      (-> text vector/not-empty?))
   :image-class  [:image--boxed]
   :meta-class   [:text--uppercase             (-> snippet-config marker :meta-color    color-class      (or :color--soft-grey))
                                               (-> snippet-config marker :meta-size     size-class       (or :text--xxs))
                                               (-> snippet-config marker :hide-meta?    visibility-class (or nil))]
   :marker-class [:text--uppercase             (-> snippet-config marker :marker-color  color-class      (or :color--soft-grey))
                                               (-> snippet-config marker :marker-size   size-class       (or :text--xxs))
                                               (-> snippet-config marker :hide-marker?  visibility-class (or nil))]
   :label-class  [:text--uppercase :text--bold (-> snippet-config marker :label-color   color-class      (or :color--hard-grey))
                                               (-> snippet-config marker :label-size    size-class       (or :text--xxs))]
   :text-class   [:text--boxed :scroll-x       (-> snippet-config marker :text-color    color-class      (or :color--hard-grey))
                                               (-> snippet-config marker :text-size     size-class       (or :text--s))]})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn extension->namespace-type
  ; @ignore
  ;
  ; @param (string) extension
  ; "clj", "cljc", "cljs"
  ;
  ; @usage
  ; (extension->namespace-type "clj")
  ; =>
  ; "Clojure namespace"
  ;
  ; @return (string)
  [extension]
  (case extension "clj" "Clojure namespace" "cljc" "Isomorphic namespace" "cljs" "ClojureScript namespace" "Unknown"))

(defn extension->namespaces-type
  ; @ignore
  ;
  ; @param (string) extension
  ; "clj", "cljc", "cljs"
  ;
  ; @usage
  ; (extension->namespaces-type "clj")
  ; =>
  ; "Clojure namespaces"
  ;
  ; @return (string)
  [extension]
  (if-let [namespace-type (extension->namespace-type extension)]
          (str namespace-type "s")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn page-title
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @usage
  ; (page-title [...] {:library {:name "my-library" :version "1.0.0" ...} ...} {...})
  ; =>
  ; "my-library 1.0.0 documentation"
  ;
  ; @return (string)
  [_ options _]
  (string/join [(-> options :library :name)
                (-> options :library :version)
                (-> "documentation")]
               " " {:join-empty? false}))

(defn symbol-anchor
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ;
  ; @return (string)
  [_ _ _ declaration]
  (-> declaration :name hiccup/value (string/prepend "#")))

(defn namespace-path
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (string)
  [_ _ file-data extension]
  (-> file-data :ns-map :declaration :name (string/replace-part "." "/")
                                           (string/prepend      "/" extension "/")
                                           (string/append       ".html")))

(defn namespace-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (string)
  [state {:keys [base-uri] :as options} file-data extension]
  (let [namespace-path (namespace-path state options file-data extension)]
       (str base-uri namespace-path)))

(defn preview-image-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) snippet
  ;
  ; @return (string)
  [_ {:keys [previews-uri]} snippet]
  (let [preview-path (-> snippet :meta first)]
       (str previews-uri "/" preview-path)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn filter-sections
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (keyword) type
  ;
  ; @usage
  ; (filter-sections [...] {...} {:sections [...]} :tutorial)
  ;
  ; @return (maps in vector)
  [state _ file-data type]
  (letfn [(f0 [%] (-> % :type (= type)))]
         (-> file-data :sections (vector/keep-items-by f0)
                                 (vector/to-nil {:if-empty? true}))))

(defn filter-namespaces
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (string) extension
  ;
  ; @usage
  ; (filter-namespaces [{:filepath "source-code/my_namespace_a.clj"  ...}
  ;                     {:filepath "source-code/my_namespace_a.cljs" ...}]
  ;                    {...}
  ;                    "clj")
  ; =>
  ; [{:filepath "source-code/my_namespace_a.clj" ...}]
  ;
  ; @return (maps in vector)
  [state _ extension]
  (letfn [(f0 [%] (-> % :filepath io/filepath->extension (= extension)))]
         (-> state (vector/keep-items-by f0)
                   (vector/to-nil {:if-empty? true}))))

(defn sort-namespaces
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @usage
  ; (sort-namespaces [{:ns-map {:declaration {:name "my-namespace"}}      ...}
  ;                   {:ns-map {:declaration {:name "another-namespace"}} ...}]
  ;                  {...})
  ; =>
  ; [{:ns-map {:declaration {:name "another-namespace"}} ...}
  ;  {:ns-map {:declaration {:name "my-namespace"}}      ...}]
  ;
  ; @return (maps in vector)
  [state _]
  (letfn [(f0 [%] (-> % :ns-map :declaration :name))]
         (-> state (vector/sort-items-by f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn sort-section-content
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; {:snippet-order (map)(opt)
  ;   {:def (keywords in vector)(opt)
  ;    :defn (keywords in vector)(opt)
  ;    :tutorial (keywords in vector)(opt)}
  ;  ...}
  ; @param (map) section
  ; {:content (maps in vector)
  ;  :type (keyword)
  ;  ...}
  ;
  ; @usage
  ; (sort-section-content [...] {...} {:content [...] :type :tutorial :snippet-order {:tutorial [...] ...} ...})
  ; =>
  ; {:content [...]
  ;  ...}
  ;
  ; @return (map)
  [_ {:keys [snippet-order]} {:keys [type] :as section}]
  (letfn [(f0 [%]   (vector/sort-items-by % f1 :marker))
          (f1 [a b] (vector/order-comparator (type snippet-order) a b))]
         (if (-> snippet-order type vector?)
             (-> section (update :content f0))
             (-> section))))

(defn append-declaration-source-code
  ; @ignore
  ;
  ; @description
  ; Appends the imported source code of declaration as a snippet of the given declaration.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ; {:content (maps in vector)
  ;  :source-code (string)
  ;  ...}
  ;
  ; @usage
  ; (append-declaration-source-code [...] {...} {:content [...] :source-code "(defn ...)" :type :defn ...})
  ; =>
  ; {:content     [{:marker :*source-code* :text ["(defn ...)"]} ...]
  ;  :source-code "..."
  ;  :type        :defn
  ;  ...}
  ;
  ; @return (map)
  [_ _ {:keys [source-code] :as section}]
  (let [snippet {:marker :*source-code* :text [source-code]}]
       (-> section (update :content vector/cons-item snippet))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn parse-link
  ; @ignore
  ;
  ; @param (string) n
  ; @param (integer) position
  ;
  ; @usage
  ; (parse-link "My text [My link](#my-anchor)")
  ; =>
  ; ["My text" [:a {:href "#my-anchor"} "My text"]]
  ;
  ; @param (vector)
  [n position]
  (letfn [(f0 [%] (regex/re-first % #"(?<=\[)[^\[\]]+(?=\]\()"))
          (f1 [%] (regex/re-first % #"(?<=\]\()[^\(\)]+(?=\))"))]
         (let [link (-> n (string/keep-range position)
                          (string/to-first-occurence ")"))]
              [(string/keep-range n 0 position)
               [:a {:class [:inline-link :color--hard-blue] :href (f1 link)} (-> link f0 string/trim)]
               (string/keep-range n (+ position (count link)))])))

(defn parse-links
  ; @ignore
  ;
  ; @param (vector) n
  ;
  ; @usage
  ; (parse-links ["My text [My anchor](#my-anchor)" [:br] ...])
  ; =>
  ; ["My text" [:a {:href "#my-anchor"} "My text"] [:br] ...]
  ;
  ; @param (vector)
  [n]
  (letfn [(f0 [result %]
              (if-let [position (regex/first-dex-of % #"\[[^\[\]]+\]\([^\(\)\h]+\)")]
                      (vector/concat-items result (parse-links (parse-link % position)))
                      (vector/conj-item    result %)))]
         (reduce f0 [] n)))

(defn unparse-html
  ; @ignore
  ;
  ; @param (vector) n
  ;
  ; @usage
  ; (unparse-html ["This is an HTML element: <img />" [:br] ...])
  ; =>
  ; ["This is an HTML element: &lt;img /&gt;" [:br] ...]
  ;
  ; @param (vector)
  [n]
  (letfn [(f0 [%]
              (if (-> % string?)
                  (-> % (string/replace-part "<" "&lt;")
                        (string/replace-part ">" "&gt;"))
                  (-> %)))]
         (vector/->items n f0)))
