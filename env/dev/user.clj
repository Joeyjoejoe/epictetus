(ns user
  (:require [epictetus.core :refer [reg-event reg-u reg-eu]]
            [epictetus.coeffect :as cofx]
            [epictetus.utils.buffer :as util]
            [clojure.java.io :as io]
            [epictetus.state :as state]
            [epictetus.dev :refer [start stop resume reset]])


  (:import (org.lwjgl.glfw GLFW)
           (org.joml Matrix4f Vector3f)
           (org.lwjgl BufferUtils)
           (org.lwjgl.opengl GL45))
  )


;; TODO Poor naming
(defn time-back-and-forth
  [t duration]
  (let [cnt    (/ t duration)
        elapse (mod t duration)
        percent (/ (* elapse 100.0)
                   duration)]
    (/ (if (even? (int cnt))
         percent
         (- 100 percent))
       100)))



(defn rotate-around
  [mat angle]
  (let [center (Vector3f. 1.0 0.0 0.0)]
    (-> mat
        (.translate center)
        (.rotateY (. Math toRadians angle))
        (.translate (.negate center)))))




;;  Vector3f center = new Vector3f(0.0f, 3.0f, 4.0f);
;;  Vector3f pointToRotate = new Vector3f(0.0f, 4.0f, 4.0f);
;;  new Matrix4f().translate(center)
;;                .rotate((float) Math.toRadians(90.0f), 1.0f, 0.0f, 0.0f)
;;                .translate(center.negate())
;;                .transformPosition(pointToRotate);

;;
;;   (let [[x  y]  (:position entity)
;;         scale   (or (:scale entity) 1.0)
;;         [xc yc] (mapv #(* % scale) center)
;;         c  (Math/cos (. Math toRadians angle))
;;         s  (Math/sin (. Math toRadians angle))
;;         xt (- x xc)
;;         yt (- y yc)
;;         xr (- (* xt c) (* yt s))
;;         yr (+ (* xt s) (* yt c))
;;         xf (+ xr xc)
;;         yf (+ yr yc)]))

;; Camera uniforms
(reg-eu :model
        (fn model-matrix [db entities entity]
          (let [{:keys [position rotation scale], :or {scale 1.0}} entity
                t (GLFW/glfwGetTime)
                [x y z] position
                [rx ry rz] rotation
                buffer (BufferUtils/createFloatBuffer 16)]

            (-> (Matrix4f.)
                (.translate x y z)
                (rotate-around (* (time-back-and-forth t 0.5) 15.0))
               ;;  (.rotateY (time-back-and-forth t 0.5))
               ;;  (.rotateX (time-back-and-forth t 0.9))
                (.scale scale scale 1.0)
                (.get buffer)))))

(reg-u :view
       (fn [db]
         (let [t (GLFW/glfwGetTime)
               eye-x 0.0 ;; (* 10.0 (Math/sin t))
               eye-y 0.0 ;; (* -10.0 (Math/cos t))
               eye-z 25.0 ;; (* 10.0 (Math/cos t))
               center-x 0.0
               center-y 0.0
               center-z 0.0
               up-x     0.0
               up-y     1.0
               up-z     0.0
               buffer (BufferUtils/createFloatBuffer 16)]
           (-> (Matrix4f.)
               (.lookAt eye-x eye-y eye-z
                        center-x center-y center-z
                        up-x up-y up-z)
               (.get buffer)))))

(reg-u :projection
       (fn [db]
         (let [window (:glfw/window @state/system)
               width  (util/int-buffer [0])
               height (util/int-buffer [0])
               _      (GLFW/glfwGetWindowSize window width height)
               fovy   (. Math toRadians 45.0)
               aspect (/ (.get width 0) (.get height 0))
               zmin   0.01
               zmax   100.0
               buffer (BufferUtils/createFloatBuffer 16)]
           (-> (Matrix4f.)
               (.perspective fovy aspect zmin zmax)
               (.get buffer)))))

(reg-eu :t
        (fn [db entities entity]
          (GLFW/glfwGetTime)))

(reg-eu :speed
        (fn [db entities entity]
          (or (:speed entity)
              0.0)))

(reg-eu :textIndex0
        (fn [db entities entity]
          (if-let [textures (get-in entity [:assets :textures])]
            (do
              (GL45/glBindTextureUnit 0 (first textures))
              0))))

(reg-eu :animIndex
        (fn [db entities entity]
          (if-let [start (:anim/start entity)]
            (let [{:keys [:anim/duration :anim/frames]} entity
                  now (get-in db [:loop/iteration :time/current])
                  frame-duration (/ duration frames)
                  elapsed        (- now start)
                  frame-nth      (Math/floor (/ elapsed frame-duration))

                  n (-> frame-nth
                        (mod frames)
                        int)]
              (println :index n)
              n)
            6)))

(reg-event
  :mouse/position
  (fn [{[_ position] :event} fx]
    (assoc-in fx [:db :mouse/position] position)))

(reg-event
  [:press :delete]
  (fn remove-cube [_ fx]
    (assoc fx :delete-all true)))

(reg-event
  [:press :space]
  [(cofx/inject :edn/load "hero-2.edn")]
  (fn render-level [{model :edn/load} fx]
    (-> fx
        (update :render conj [:hero model]))))

(reg-event
  [:repeat :space]
  [(cofx/inject :edn/load "cube.edn")]
  (fn render-random-cube [{model :edn/load} fx]
    (let [[min max] [-10 10]
          position  [(+ (rand min) (rand max))
                     (+ (rand min) (rand max))
                     (+ (rand min) (rand max))]
          [min max] [0 1]
          color     [(+ (rand min) (rand max))
                     (+ (rand min) (rand max))
                     (+ (rand min) (rand max))]
          colored-vertices (mapv #(assoc % :color color)
                                 (get-in model [:assets :vertices]))
          entity-id (-> "cube-"
                        (str (. (new java.util.Date) (getTime))))

          texture (-> "textures" io/resource io/file .list rand-nth)

          random-cube [entity-id (-> model
                                     (assoc :position position)
                                     (assoc :scale (rand 1.0))
                                     (assoc :speed (* (rand 100) (rand-nth [-1 1])))
                                     (assoc-in [:assets :textures] [(str "textures/" texture)])
                                     (assoc-in [:assets :vertices] colored-vertices))]]
      (-> fx
          (update :render conj random-cube)))))


(reg-event [:press :s]
           (fn move-right [_ fx]
             (let [[x y z] (get-in @state/entities [:hero :position])]
               (assoc fx :entity/set [:hero {:motion   [:walk :right]
                                             :position [x y (+ z 0.15)]
                                             :anim/start (GLFW/glfwGetTime)}]))))
(reg-event [:repeat :s]
           (fn move-right [_ fx]
             (let [[x y z] (get-in @state/entities [:hero :position])]
               (assoc fx :entity/set [:hero {:position [(- x 0.005) (- y 0.005) (+ z 0.15)]}]))))

(reg-event [:release :s]
           (fn move-left [_ fx]
             (assoc fx :entity/set [:hero {:motion nil
                                           :anim/start nil}])))
(comment

  (defn spriteCoords [sprite row col]
    (let [{:keys [fwidth fheight w h]} sprite
          x (/ (* col fwidth) w)
          y (/ (* row fheight) h)]
      [x y]))

  (let [w         1142
        h         635
        row       2
        cols      [0 1 2 3 4 5 6 7 8 9 10 11]
        fwidth     (/ 95.16 w)
        fheight    (/ 158.75 h)
        A (mapv   #(spriteCoords {:fwidth fwidth :fheight fheight :w w :h h} row %) cols)
        B (reduce #(conj %1 [(first %2) (+ fheight (last %2))]) [] A)
        C (reduce #(conj %1 [(+ fwidth (first %2)) (+ fheight (last %2))]) [] A)
        D (reduce #(conj %1 [(+ fwidth (first %2)) (last %2)]) [] A)]
    D)

  state/db

  state/system

  (get @state/entities :hero)


  )
