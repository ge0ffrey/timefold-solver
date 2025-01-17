package ai.timefold.solver.benchmark.impl.statistic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;

import ai.timefold.solver.benchmark.config.statistic.ProblemStatisticType;
import ai.timefold.solver.benchmark.impl.report.BenchmarkReport;
import ai.timefold.solver.benchmark.impl.report.ReportHelper;
import ai.timefold.solver.benchmark.impl.result.ProblemBenchmarkResult;
import ai.timefold.solver.benchmark.impl.result.SingleBenchmarkResult;
import ai.timefold.solver.benchmark.impl.result.SubSingleBenchmarkResult;
import ai.timefold.solver.benchmark.impl.statistic.bestscore.BestScoreProblemStatistic;
import ai.timefold.solver.benchmark.impl.statistic.bestsolutionmutation.BestSolutionMutationProblemStatistic;
import ai.timefold.solver.benchmark.impl.statistic.common.GraphSupport;
import ai.timefold.solver.benchmark.impl.statistic.memoryuse.MemoryUseProblemStatistic;
import ai.timefold.solver.benchmark.impl.statistic.movecountperstep.MoveCountPerStepProblemStatistic;
import ai.timefold.solver.benchmark.impl.statistic.scorecalculationspeed.ScoreCalculationSpeedProblemStatistic;
import ai.timefold.solver.benchmark.impl.statistic.stepscore.StepScoreProblemStatistic;

import org.jfree.chart.JFreeChart;

/**
 * 1 statistic of {@link ProblemBenchmarkResult}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
        BestScoreProblemStatistic.class,
        StepScoreProblemStatistic.class,
        ScoreCalculationSpeedProblemStatistic.class,
        BestSolutionMutationProblemStatistic.class,
        MoveCountPerStepProblemStatistic.class,
        MemoryUseProblemStatistic.class
})
public abstract class ProblemStatistic {

    @XmlTransient // Bi-directional relationship restored through BenchmarkResultIO
    protected ProblemBenchmarkResult<Object> problemBenchmarkResult;

    protected final ProblemStatisticType problemStatisticType;

    // ************************************************************************
    // Report accumulates
    // ************************************************************************

    protected List<String> warningList = null;

    public ProblemStatistic() {
        problemStatisticType = null;
    }

    protected ProblemStatistic(ProblemBenchmarkResult problemBenchmarkResult, ProblemStatisticType problemStatisticType) {
        this.problemBenchmarkResult = problemBenchmarkResult;
        this.problemStatisticType = problemStatisticType;
    }

    public ProblemBenchmarkResult getProblemBenchmarkResult() {
        return problemBenchmarkResult;
    }

    public void setProblemBenchmarkResult(ProblemBenchmarkResult problemBenchmarkResult) {
        this.problemBenchmarkResult = problemBenchmarkResult;
    }

    public ProblemStatisticType getProblemStatisticType() {
        return problemStatisticType;
    }

    public String getAnchorId() {
        return ReportHelper.escapeHtmlId(problemBenchmarkResult.getName() + "_" + problemStatisticType.name());
    }

    public List<String> getWarningList() {
        return warningList;
    }

    public List<SubSingleStatistic> getSubSingleStatisticList() {
        List<SingleBenchmarkResult> singleBenchmarkResultList = problemBenchmarkResult.getSingleBenchmarkResultList();
        List<SubSingleStatistic> subSingleStatisticList = new ArrayList<>(singleBenchmarkResultList.size());
        for (SingleBenchmarkResult singleBenchmarkResult : singleBenchmarkResultList) {
            if (singleBenchmarkResult.getSubSingleBenchmarkResultList().isEmpty()) {
                continue;
            }
            // All subSingles have the same sub single statistics
            subSingleStatisticList.add(singleBenchmarkResult.getSubSingleBenchmarkResultList().get(0)
                    .getEffectiveSubSingleStatisticMap().get(problemStatisticType));
        }
        return subSingleStatisticList;
    }

    public abstract SubSingleStatistic createSubSingleStatistic(SubSingleBenchmarkResult subSingleBenchmarkResult);

    // ************************************************************************
    // Write methods
    // ************************************************************************

    public void accumulateResults(BenchmarkReport benchmarkReport) {
        warningList = new ArrayList<>();
        fillWarningList();
    }

    public abstract void writeGraphFiles(BenchmarkReport benchmarkReport);

    protected void fillWarningList() {
    }

    protected File writeChartToImageFile(JFreeChart chart, String fileNameBase) {
        File chartFile = new File(problemBenchmarkResult.getProblemReportDirectory(), fileNameBase + ".png");
        GraphSupport.writeChartToImageFile(chart, chartFile);
        return chartFile;
    }

    public File getGraphFile() {
        List<File> graphFileList = getGraphFileList();
        if (graphFileList == null || graphFileList.isEmpty()) {
            return null;
        } else if (graphFileList.size() > 1) {
            throw new IllegalStateException("Cannot get graph file for the ProblemStatistic (" + this
                    + ") because it has more than 1 graph file. See method getGraphList() and "
                    + ProblemStatisticType.class.getSimpleName() + ".hasScoreLevels()");
        } else {
            return graphFileList.get(0);
        }
    }

    public abstract List<File> getGraphFileList();

    @Override
    public String toString() {
        return problemBenchmarkResult + "_" + problemStatisticType;
    }

}
