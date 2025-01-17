package ai.timefold.solver.constraint.streams.bavet.uni;

import java.util.List;
import java.util.Set;

import ai.timefold.solver.constraint.streams.bavet.BavetConstraintFactory;
import ai.timefold.solver.constraint.streams.bavet.bi.BavetGroupBiConstraintStream;
import ai.timefold.solver.constraint.streams.bavet.bi.BiTuple;
import ai.timefold.solver.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import ai.timefold.solver.constraint.streams.bavet.common.GroupNodeConstructor;
import ai.timefold.solver.constraint.streams.bavet.common.NodeBuildHelper;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintStream;

final class BavetBiGroupBridgeUniConstraintStream<Solution_, A, NewA, NewB>
        extends BavetAbstractUniConstraintStream<Solution_, A> {

    private final BavetAbstractUniConstraintStream<Solution_, A> parent;
    private BavetGroupBiConstraintStream<Solution_, NewA, NewB> groupStream;
    private final GroupNodeConstructor<BiTuple<NewA, NewB>> nodeConstructor;

    public BavetBiGroupBridgeUniConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractUniConstraintStream<Solution_, A> parent,
            GroupNodeConstructor<BiTuple<NewA, NewB>> nodeConstructor) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
        this.nodeConstructor = nodeConstructor;
    }

    @Override
    public boolean guaranteesDistinct() {
        return true;
    }

    public void setGroupStream(BavetGroupBiConstraintStream<Solution_, NewA, NewB> groupStream) {
        this.groupStream = groupStream;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        parent.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        List<? extends ConstraintStream> groupStreamChildList = groupStream.getChildStreamList();
        nodeConstructor.build(buildHelper, parent.getTupleSource(), groupStream, groupStreamChildList, this, childStreamList,
                constraintFactory.getEnvironmentMode());
    }

    @Override
    public BavetAbstractConstraintStream<Solution_> getTupleSource() {
        return parent.getTupleSource();
    }

}
