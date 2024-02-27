
(ns source-code-documentation.api
    (:require [source-code-documentation.core.engine :as core.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Constant headers
;
; @---
; Last coherent comment row groups preceding constant declarations are constant headers.
;
; @example
; ;; @description
; ;; Lorem ipsum dolor sit amet.
; ;;
; ;; @constant (string)
; (def MY-CONSTANT ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Function headers
;
; @---
; Last coherent comment row groups preceding function argument lists are function headers.
;
; @example
; (defn my-function
;   ;; @description
;   ;; Lorem ipsum dolor sit amet.
;   ;;
;   ;; @param (integer) a
;   ;; @param (integer)(opt) b
;   ;;
;   ;; @return (integer)
;   [_] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Tutorials
;
; @---
; First coherent comment row groups following tutorial markers are tutorial contents.
;
; @example
; (ns my-namespace)
;
; ;; @tutorial My tutorial
; ;;
; ;; Lorem ipsum dolor sit amet.
; ;;
; ;; @usage
; ;; (my-function ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Sections
;
; @---
; Tutorial contents, constant headers and function headers are sections.
; Sections contain snippets delimited by markers.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Markers
;
; @---
; Markers create snippets containing the content below the marker.
;
; Each snippet has a label bar with:
; - the marker name
; - the optional snippet label
; - two optional meta values of the snippet
;
; @my-marker (meta#1)(meta#2) My snippet
; The marker of this very snippet:
;
; ;; @my-marker (meta#1)(meta#2) My snippet
;
; @---
; Markers can contain digits, hyphens and lowercase letters:
;
; ;; @my-marker123
;
; @---
; Predifined (reserved) markers:
;
; ;; @redirect
; ;; @link
; ;; @ignore

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Customizing snippets
;
; @---
; (defn my-function
;   ;; @my-marker
;   ;; Lorem ipsum dolor sit amet.
;   [_] ...)
;
; @---
; (generate-documentation! {:snippet-config {:my-marker {:collapsible? true
;                                                        :marker-color :muted
;                                                        :label-color  :primary ...}
;                           ...}})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Custom snippet order
;
; @---
; (defn my-function
;   ;; @my-marker
;   ;; Lorem ipsum dolor sit amet.
;   ;;
;   ;; @another-marker
;   ;; Lorem ipsum dolor sit amet.
;   [_] ...)
;
; @---
; (generate-documentation! {:snippet-order {:defn     [:*source-code* :another-marker :my-marker ...]
;                                           :def      [:*source-code* :another-marker :my-marker ...]
;                                           :tutorial [               :another-marker :my-marker ...]}
;                           ...}})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Preview images
;
; @---
; Snippets can display a preview image.
; The URI of the preview image must be provided as the first meta value of the snippet that must end with an image type extension.
;
; @---
; (defn my-function
;   ;; @my-marker (my-image.png)
;   ;; Lorem ipsum dolor sit amet.
;   [_] ...)
;
; @---
; The ':previews-uri' property (optional) is prepended to preview paths as a base URI.
;
; (generate-documentation! {:previews-uri "https://github.com/author/my-library/blob/main/previews"
;                           ...}})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Ignored snippets
;
; @---
; Snippets below an '@ignore' marker are removed from the documentation output.
;
; @---
; (defn my-function
;   ;; @description
;   ;; Lorem ipsum dolor sit amet.
;   ;;
;   ;; @ignore
;   ;; Everything below this marker is ignored.
;   ;;
;   ;; @return (map)
;   ;; This snippet is ignored as well.
;   [_] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Redirected sections
;
; Redirected sections are replaced with the content of the target section.
;
; @--- Redirected tutorial within the same namespace:
; ;; @tutorial My tutorial
; ;; Lorem ipsum dolor sit amet.
;
; ;; @tutorial Another tutorial
; ;; @redirect (my-tutorial)
;
; @--- Redirected constant header within the same namespace:
; ;; @description
; ;; Lorem ipsum dolor sit amet.
; (def MY-CONSTANT {})
;
; ;; @redirect (MY-CONSTANT)
; (def ANOTHER-CONSTANT {})
;
; @--- Redirected function header within the same namespace:
; (defn my-function
;   ;; @description
;   ;; Lorem ipsum dolor sit amet.
;   [_] ...)
;
; (defn another-function
;   ;; @redirect (my-function)
;   [_] ...)
;
; @--- Redirected tutorial from remote namespace:
; (ns my-namespace)
;
; ;; @tutorial My tutorial
; ;; @redirect (another-namespace/another-tutorial)
;
; @--- Redirected constant header from remote namespace:
; (ns my-namespace)
;
; ;; @redirect (another-namespace/ANOTHER-CONSTANT)
; (def MY-CONSTANT {})
;
; @--- Redirected function header from remote namespace:
; (ns my-namespace)
;
; (defn my-function
;   ;; @redirect (another-namespace/another-function)
;   [_] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Linked sections
;
; Linked sections are insterted into the original section where the '@link' marker was placed.
;
; @--- Linked tutorial within the same namespace:
; ;; @tutorial My tutorial
; ;; Lorem ipsum dolor sit amet.
;
; ;; @tutorial Another tutorial
; ;; Consectetur adipisicing elit.
; ;;
; ;; @link (my-tutorial)
;
; @--- Linked constant header within the same namespace:
; ;; @description
; ;; Lorem ipsum dolor sit amet.
; (def MY-CONSTANT {})
;
; ;; @description
; ;; Consectetur adipisicing elit.
; ;;
; ;; @link (MY-CONSTANT)
; (def ANOTHER-CONSTANT {})
;
; @--- Linked function header within the same namespace:
; (defn my-function
;   ;; @description
;   ;; Lorem ipsum dolor sit amet.
;   [_] ...)
;
; (defn another-function
;   ;; @description
;   ;; Consectetur adipisicing elit.
;   ;;
;   ;; @link (my-function)
;   [_] ...)
;
; @--- Linked tutorial from remote namespace:
; (ns my-namespace)
;
; ;; @tutorial My tutorial
; ;; Lorem ipsum dolor sit amet.
; ;;
; ;; @link (another-namespace/another-tutorial)
;
; @--- Linked constant header from remote namespace:
; (ns my-namespace)
;
; ;; @description
; ;; Lorem ipsum dolor sit amet.
; ;;
; ;; @link (another-namespace/ANOTHER-CONSTANT)
; (def MY-CONSTANT {})
;
; @--- Linked function header from remote namespace:
; (ns my-namespace)
;
; (defn my-function
;   ;; @description
;   ;; Lorem ipsum dolor sit amet.
;   ;;
;   ;; @link (another-namespace/another-function)
;   [_] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Wildcards in pointers
;
; Wildcards work only if the redirection corresponds to a 'def' declaration, and the declaration has a symbol type value
; that can be used to derive a name or namespace from it.
;
; @---
; (ns my-namespace)
;
; ;; @redirect (*)
; ;; '*' => 'my-namespace/ANOTHER-CONSTANT'
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; @---
; (ns my-namespace)
;
; ;; @redirect (*/*)
; ;; '*/*' => 'another-namespace/ANOTHER-CONSTANT'
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; @---
; (ns my-namespace)
;
; ;; @redirect (*/bar)
; ;; '*/bar' => 'another-namespace/bar'
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; @---
; (ns my-namespace)
;
; ;; @redirect (foo/*)
; ;; 'foo/*' => 'foo/ANOTHER-CONSTANT'
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Highlighted comments
;
; @---
; Parts of snippet rows that are commented with a double semicolon are highlighted in the output.
;
; Lorem ipsum ;; dolor sit amet
;
; @---
; There is no requirement to use double semicolons in declaration headers or tutorial contents.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Links and anchors
;
; @---
; Place external links and anchors anywhere in your documentation content.
;
; (defn my-function
;   ;; @description
;   ;; Lorem ipsum dolor set amit.
;   ;;
;   ;; [Link label](https://... )
;   ;; [Anchor label](#... )
;   ;;
;   ;; @return (integer)
;   [_] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (source-code-documentation.core.engine/*)
(def generate-documentation! core.engine/generate-documentation!)
