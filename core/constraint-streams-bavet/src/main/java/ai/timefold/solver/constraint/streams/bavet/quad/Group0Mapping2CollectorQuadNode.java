package ai.timefold.solver.constraint.streams.bavet.quad;

import ai.timefold.solver.constraint.streams.bavet.bi.BiTuple;
import ai.timefold.solver.constraint.streams.bavet.bi.BiTupleImpl;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.quad.QuadConstraintCollector;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.util.Pair;

final class Group0Mapping2CollectorQuadNode<OldA, OldB, OldC, OldD, A, B, ResultContainerA_, ResultContainerB_>
        extends AbstractGroupQuadNode<OldA, OldB, OldC, OldD, BiTuple<A, B>, BiTupleImpl<A, B>, Void, Object, Pair<A, B>> {

    private final int outputStoreSize;

    public Group0Mapping2CollectorQuadNode(int groupStoreIndex, int undoStoreIndex,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerA_, A> collectorA,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerB_, B> collectorB,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, null, mergeCollectors(collectorA, collectorB), nextNodesTupleLifecycle,
                environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <OldA, OldB, OldC, OldD, A, B, ResultContainerA_, ResultContainerB_>
            QuadConstraintCollector<OldA, OldB, OldC, OldD, Object, Pair<A, B>> mergeCollectors(
                    QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerA_, A> collectorA,
                    QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerB_, B> collectorB) {
        return (QuadConstraintCollector<OldA, OldB, OldC, OldD, Object, Pair<A, B>>) ConstraintCollectors.compose(collectorA,
                collectorB, Pair::of);
    }

    @Override
    protected BiTupleImpl<A, B> createOutTuple(Void groupKey) {
        return new BiTupleImpl<>(null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTupleImpl<A, B> outTuple, Pair<A, B> result) {
        outTuple.factA = result.getKey();
        outTuple.factB = result.getValue();
    }

}
