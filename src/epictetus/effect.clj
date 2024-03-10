(ns epictetus.effect
  (:require [epictetus.event :as event]
            [epictetus.state :as state]
            [epictetus.texture :as textures]
            [epictetus.vertices :as vertices]
            [epictetus.interceptors :refer [->interceptor]]
            [clojure.pprint :refer [pprint]])
  (:import (org.lwjgl.glfw GLFW)))

(defn reg-fx
  "A fx is a function that takes the coeffects map and
  an optional parameter, and return a modified version
  of the coeffects map"
  [id fx-fn]
  (event/register :effect id fx-fn))

(def do-fx
  (->interceptor
    :id    :effects
    :after (fn do-all-fx
             [context]
             (let [effects               (:effects context)
                   effects-without-db (dissoc effects :db)]
               ;; :db effect is guaranteed to be handled before all other effects.
               (when-let [new-db (:db effects)]
                 ((event/get-handler :effect :db) new-db))

               (doseq [[effect-key effect-value] effects-without-db]
                 (if-let [effect-fn (event/get-handler :effect effect-key)]
                   (effect-fn effect-value)
                   (println "no handler registered for effect:" effect-key ". Ignoring.")))))))

;; CORE EFFECTS

(reg-fx :db
        (fn update-db! [new-db]
          (reset! state/db new-db)))

(reg-fx :system
        (fn add-data-to-system [data]
          (swap! state/system merge data)))

(reg-fx :dispatch
        (fn dispatch-event! [events]
          (doseq [e events]
            (event/dispatch e))))

(reg-fx :render
        (fn render!
          ([entities]
           (doseq [[id entity] entities]
             (render! id entity)))

          ([id {:as entity :keys [program]}]
           ;; TODO Assets cache (VBO duplication prevention & instance rendering)
           (let [{layout :layout} (get-in @state/system [:gl/engine :program program])
                 vao              (get-in @state/system [:gl/engine :vao layout])]
             (->> entity
                  (vertices/gpu-load! vao)
                  (textures/load-entity)
                  (swap! state/rendering assoc-in [layout program id]))))))


(reg-fx :delete
       (fn delete-entity!
         [entity-keys]
         ;; TODO update path must be obtainable from entity-key
         ;;      - Refactor state/rendering to vaoID->programID->entityIDS
         ;;      - Add a global registery for vaos, programs and entities data (integrant system)
         (apply swap! state/rendering update-in [:vao/static :default] dissoc entity-keys)))

(reg-fx :delete-all
       (fn delete-all [_]
         (reset! state/rendering {})))

;; TODO
;; (reg-fx :entity/update-in
;;         (fn assoc-in-entity [ks v]
;;           (swap! state/rendering assoc-in ks v)))

(reg-fx :loop/pause
        (fn pause-loop [_]
          (let [window  (get @state/system :glfw/window)]
            (GLFW/glfwSetWindowShouldClose window true))))
