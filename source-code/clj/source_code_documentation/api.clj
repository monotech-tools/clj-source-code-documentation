
(ns source-code-documentation.api
    (:require [source-code-documentation.core.engine :as core.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial How to place and format documentation content in Clojure files?
;
; Tutorials:
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; Lorem ipsum dolor sit amet.
; ;
; ; @usage
; ; (my-function ...)
; @---
;
; Constant declaration headers:
;
; @code
; ; @description
; ; Lorem ipsum dolor sit amet.
; ;
; ; @constant (string)
; (def MY-CONSTANT "...")
;
; Function declaration headers:
;
; @code
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @param (integer) a
;   ; @param (integer)(opt) b
;   ;
;   ; @return (integer)
;   [a & [b]] ...)



; @tutorial Markers
;
; Feel free to use block markers in any combination.
;
; @title Tutorials
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ; - Place your tutorials in your source code files.
; ; - Tutorials end at the first following empty non-comment line.
; ; - Other content block markers could be used within a tutorial.
; ;
; ; @description
; ; This description is also part of the tutorial.
; ;
; ; @code
; ; Code blocks end at the first following empty comment line.
; ;
; ; @code
; ; But code blocks can contain empty comment lines in case of the block end marker
; ; tells the interpreter where does the code block end.
; ;
; ; @---
; ;
; ; @usage
; ; Usage blocks are simply code blocks with the title "Usage".
; ;
; ; @title My title
; ; Lorem ipsum dolor sit amet.
; ;
; ; ...
; @---
;
; @title Def declarations
;
; @code
; (ns my-namespace)
;
; ; @atom (map)
; (def MY-ATOM (atom {}))
;
; ; @constant (map)
; (def MY-CONSTANT {})
; @---
;
; @code
; (ns my-namespace)
;
; ; @atom (map)
; ;
; ; @description
; ; Lorem ipsum dolor sit amet.
; (def MY-ATOM (atom {}))
;
; ; @constant (map)
; ;
; ; @description
; ; Lorem ipsum dolor sit amet.
; (def MY-CONSTANT {})
; @---
;
; @title Defn declarations
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   [])
; @---
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @param (keyword) a
;   ; @param (vector) b
;   ; [(string) b0
;   ;  (map)(opt) b1]
;   ; @param (map)(opt) c
;   ; {:c0 (keyword)(opt)
;   ;  :c1 (map)
;   ;   {...}}
;   ;
;   ; @return (map)
;   ; {:x (vector)
;   ;  :y (string)
;   ;  :z (map)}
;   [a b & [c]] {:x [...] :y "..." :z {...}})
; @---
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @param (string) a
;   ;
;   ; @usage
;   ; (a "abc")
;   ; =>
;   ; "def"
;   ;
;   ; @return (string)
;   ; Lorem ipsum dolor sit amet.
;   [a] "...")
; @---
;
; @title Preview images
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; @preview (my-image.png)
; ;
; ; - Place your preview images in tutorials or declaration documentations.
; ; - The 'create-documentation!' function takes the 'previews-uri' property which is an optional
; ;   base URI prepended to preview paths.
; @---
;
; @title Ignored contents
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @ignore
;   ; Contents below the ignore marker are not displayed in documentation books.
;   [])
; @---
;
; @title Titles and text contents
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; @description
; ; Lorem ipsum dolor sit amet.
; ;
; ; @important
; ; Lorem ipsum dolor sit amet.
; ;
; ; @info
; ; Lorem ipsum dolor sit amet.
; ;
; ; @note
; ; Lorem ipsum dolor sit amet.
; @---
;
; @title Code blocks
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; @code
; ; [:div {:class :my-class} "My content"]
; ;
; ; @code My label
; ; [:div {:class :my-class} "My content"]
; ;
; ; @usage
; ; [:div {:class :my-class} "My content"]
; @---
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; @code
; ; [:div {:class :my-class} "My content"]
; ;
; ; [:div {:class :another-class} "Another content"]
; ; @---
; ;
; ; @usage
; ; [:div {:class :my-class} "My content"]
; ;
; ; [:div {:class :another-class} "Another content"]
; ; @---
; @---
;
; @title Bug markers
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @bug (#0001)
;   ; Lorem ipsum dolor sit amet.
;   [a] ...)
; @---
;
; @code
; (ns another-namespace)
;
; (defn another-function
;   ; @bug (my-namespace/#0001)
;   [a] ...)
; @---
;
; @title Redirected documentations
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ; @redirect (another-namespace/another-tutorial)
; @---
;
; @code
; (ns my-namespace)
;
; ; @redirect (another-namespace/ANOTHER-CONSTANT)
; (def MY-CONSTANT {})
; @---
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @redirect (another-namespace/another-function)
;   [a b & [c]] ...)
; @---
;
; @title Linked documentations
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; Linked contents are simply inserted into the original content, unlike redirected contents
; ; where the target content replaces the original content.
; ;
; ; @link (another-namespace/another-tutorial)
; @---
;
; @code
; (ns my-namespace)
;
; ; @constant (map)
; ; @link (another-namespace/ANOTHER-CONSTANT)
; (def MY-CONSTANT {})
; @---
;
; @code
; (ns my-namespace)
;
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @link (another-namespace/another-function)
;   [a b & [c]] ...)
; @---
;
; @title Link and redirection pointers
;
; @code
; (ns my-namespace
;     (:require [another-namespace]))
;
; ; @redirect (bar)
; ; The 'bar' pointer points to the documentation of the 'bar' declaration within the 'my-namespace' namespace.
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; ; @redirect (foo/bar)
; ; The 'foo/bar' pointer points to the documentation of the 'bar' declaration within the 'foo' namespace.
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
; @---
;
; @title Wildcards in link and redirection pointers
;
; Wildcards work only if the pointer corresponds to a 'def' declaration, and the declaration has a symbol type value that can be used to
; derive a name or namespace from it.
;
; @code
; ; @redirect (*)
; ; The '*' pointer points to the documentation of the 'ANOTHER-CONSTANT' declaration within the 'my-namespace' namespace.
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; ; @redirect (*/*)
; ; The '*/*' pointer points to the documentation of the 'ANOTHER-CONSTANT' declaration within the 'another-namespace' namespace.
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; ; @redirect (*/bar)
; ; The '*/bar' pointer points to the documentation of the 'bar' declaration within the 'another-namespace' namespace.
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
;
; ; @redirect (foo/*)
; ; The 'foo/*' pointer points to the documentation of the 'ANOTHER-CONSTANT' declaration within the 'foo' namespace.
; (def MY-CONSTANT another-namespace/ANOTHER-CONSTANT)
; @---



; @tutorial Links
;
; @code
; (defn my-function
;   ; @description
;   ; Place a [link](https://...) or an [anchor](#another-function) anywhere in your documentation content.
;   ;
;   ; @return (integer)
;   [] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (source-code-documentation.core.engine/*)
(def generate-documentation! core.engine/generate-documentation!)
