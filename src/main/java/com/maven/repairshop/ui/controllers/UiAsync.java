package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.awt.Cursor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Window;

/**
 * Gestionnaire de tâches asynchrones (Background tasks).
 * Affiche un curseur de chargement et gère les erreurs via UiDialogs.
 */
public final class UiAsync {

    private UiAsync() {}

    public static <T> void run(Component parent, Supplier<T> task, Consumer<T> onSuccess) {
        // Astuce Pro : On change le curseur de TOUTE la fenêtre, pas juste du bouton
        Window win = SwingUtilities.getWindowAncestor(parent);
        Component target = (win != null) ? win : parent;
        
        Cursor old = target.getCursor();
        target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Sablier / Rond bleu

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                try {
                    return task.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void done() {
                try {
                    // Récupération du résultat (peut lancer une exception si erreur dans doInBackground)
                    T res = get();
                    if (onSuccess != null) {
                        onSuccess.accept(res);
                    }
                } catch (Exception ex) {
                    // C'est ici que la magie opère : ça va ouvrir ta belle fenêtre rouge !
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UiDialogs.handle(parent, cause);
                } finally {
                    // On remet le curseur normal
                    target.setCursor(old);
                }
            }
        }.execute();
    }

    public static void runVoid(Component parent, Runnable task, Runnable onSuccess) {
        run(parent, () -> {
            task.run();
            return true;
        }, ok -> {
            if (onSuccess != null) onSuccess.run();
        });
    }
}