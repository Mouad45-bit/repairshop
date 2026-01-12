package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.awt.Cursor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

public final class UiAsync {

    private UiAsync() {}

    public static <T> void run(Component parent, Supplier<T> task, Consumer<T> onSuccess) {
        Cursor old = parent.getCursor();
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return task.get();
            }

            @Override
            protected void done() {
                try {
                    T res = get();
                    onSuccess.accept(res);
                } catch (Exception ex) {
                    UiDialogs.handle(parent, ex.getCause() != null ? ex.getCause() : ex);
                } finally {
                    parent.setCursor(old);
                }
            }
        }.execute();
    }

    public static void runVoid(Component parent, Runnable task, Runnable onSuccess) {
        run(parent, () -> {
            task.run();
            return true;
        }, ok -> onSuccess.run());
    }
}