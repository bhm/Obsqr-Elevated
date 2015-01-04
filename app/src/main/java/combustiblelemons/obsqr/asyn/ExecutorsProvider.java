package combustiblelemons.obsqr.asyn;

import android.util.SparseArray;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorsProvider {

    private static SparseArray<Executor> executors = new SparseArray<Executor>();

    public static Executor getExecutor(Class<?> clss) {
        return getExecutor(clss, Type.SINGLE);
    }

    public static Executor getExecutor(Class<?> clss, Type type) {
        return getExecutor(clss.getSimpleName().hashCode(), type);
    }

    public static Executor getExecutor(int hashCode, Type type) {
        if (executors.get(hashCode) == null) {
            Executor _executor;
            if (type.equals(Type.SINGLE)) {
                _executor = Executors.newSingleThreadExecutor();
            } else {
                _executor = Executors.newCachedThreadPool();
            }
            executors.put(hashCode, _executor);
        }
        return executors.get(hashCode);
    }

    public enum Type {
        SINGLE, CACHED
    }
}
