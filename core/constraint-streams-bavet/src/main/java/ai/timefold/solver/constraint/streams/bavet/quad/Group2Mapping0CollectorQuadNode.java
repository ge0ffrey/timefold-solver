package ai.timefold.solver.constraint.streams.bavet.quad;

import ai.timefold.solver.constraint.streams.bavet.bi.BiTuple;
import ai.timefold.solver.constraint.streams.bavet.bi.BiTupleImpl;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.util.Pair;

final class Group2Mapping0CollectorQuadNode<OldA, OldB, OldC, OldD, A, B>
        extends AbstractGroupQuadNode<OldA, OldB, OldC, OldD, BiTuple<A, B>, BiTupleImpl<A, B>, Pair<A, B>, Void, Void> {

    private final int outputStoreSize;

    public Group2Mapping0CollectorQuadNode(QuadFunction<OldA, OldB, OldC, OldD, A> groupKeyMappingA,
            QuadFunction<OldA, OldB, OldC, OldD, B> groupKeyMappingB, int groupStoreIndex,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, tuple), nextNodesTupleLifecycle,
                environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, B, OldA, OldB, OldC, OldD> Pair<A, B> createGroupKey(QuadFunction<OldA, OldB, OldC, OldD, A> groupKeyMappingA,
            QuadFunction<OldA, OldB, OldC, OldD, B> groupKeyMappingB, QuadTuple<OldA, OldB, OldC, OldD> tuple) {
        OldA oldA = tuple.getFactA();
        OldB oldB = tuple.getFactB();
        OldC oldC = tuple.getFactC();
        OldD oldD = tuple.getFactD();
        A a = groupKeyMappingA.apply(oldA, oldB, oldC, oldD);
        B b = groupKeyMappingB.apply(oldA, oldB, oldC, oldD);
        return Pair.of(a, b);
    }

    @Override
    protected BiTupleImpl<A, B> createOutTuple(Pair<A, B> groupKey) {
        return new BiTupleImpl<>(groupKey.getKey(), groupKey.getValue(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTupleImpl<A, B> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
