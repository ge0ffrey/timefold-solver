package ai.timefold.solver.constraint.streams.bavet.uni;

import java.util.function.Function;

import ai.timefold.solver.constraint.streams.bavet.common.AbstractFlattenLastNode;
import ai.timefold.solver.constraint.streams.bavet.common.TupleLifecycle;

final class FlattenLastUniNode<A, NewA> extends AbstractFlattenLastNode<UniTuple<A>, UniTuple<NewA>, A, NewA> {

    private final int outputStoreSize;

    FlattenLastUniNode(int flattenLastStoreIndex, Function<A, Iterable<NewA>> mappingFunction,
            TupleLifecycle<UniTuple<NewA>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected UniTuple<NewA> createTuple(UniTuple<A> originalTuple, NewA item) {
        return new UniTupleImpl<>(item, outputStoreSize);
    }

    @Override
    protected A getEffectiveFactIn(UniTuple<A> tuple) {
        return tuple.getFactA();
    }

    @Override
    protected NewA getEffectiveFactOut(UniTuple<NewA> outTuple) {
        return outTuple.getFactA();
    }

}
