package ai.timefold.solver.benchmark.impl.statistic.subsingle.pickedmovetypestepscore;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlTransient;

import ai.timefold.solver.benchmark.config.statistic.SingleStatisticType;
import ai.timefold.solver.benchmark.impl.report.BenchmarkReport;
import ai.timefold.solver.benchmark.impl.result.SubSingleBenchmarkResult;
import ai.timefold.solver.benchmark.impl.statistic.PureSubSingleStatistic;
import ai.timefold.solver.benchmark.impl.statistic.StatisticRegistry;
import ai.timefold.solver.benchmark.impl.statistic.common.MillisecondsSpentNumberFormat;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.config.solver.monitoring.SolverMetric;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchStepScope;
import ai.timefold.solver.core.impl.score.definition.ScoreDefinition;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

public class PickedMoveTypeStepScoreDiffSubSingleStatistic<Solution_>
        extends PureSubSingleStatistic<Solution_, PickedMoveTypeStepScoreDiffStatisticPoint> {

    @XmlTransient
    protected List<File> graphFileList = null;

    PickedMoveTypeStepScoreDiffSubSingleStatistic() {
        // For JAXB.
    }

    public PickedMoveTypeStepScoreDiffSubSingleStatistic(SubSingleBenchmarkResult subSingleBenchmarkResult) {
        super(subSingleBenchmarkResult, SingleStatisticType.PICKED_MOVE_TYPE_STEP_SCORE_DIFF);
    }

    /**
     * @return never null
     */
    @Override
    public List<File> getGraphFileList() {
        return graphFileList;
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    @Override
    public void open(StatisticRegistry<Solution_> registry, Tags runTag, Solver<Solution_> solver) {
        registry.addListener(SolverMetric.PICKED_MOVE_TYPE_STEP_SCORE_DIFF, (timeMillisSpent, stepScope) -> {
            if (stepScope instanceof LocalSearchStepScope) {
                String moveType = ((LocalSearchStepScope<Solution_>) stepScope).getStep().getSimpleMoveTypeDescription();
                registry.extractScoreFromMeters(SolverMetric.PICKED_MOVE_TYPE_STEP_SCORE_DIFF,
                        runTag.and(Tag.of("move.type", moveType)),
                        score -> pointList.add(new PickedMoveTypeStepScoreDiffStatisticPoint(
                                timeMillisSpent, moveType, score)));
            }
        });
    }

    // ************************************************************************
    // CSV methods
    // ************************************************************************

    @Override
    protected String getCsvHeader() {
        return PickedMoveTypeStepScoreDiffStatisticPoint.buildCsvLine("timeMillisSpent", "moveType", "stepScoreDiff");
    }

    @Override
    protected PickedMoveTypeStepScoreDiffStatisticPoint createPointFromCsvLine(ScoreDefinition<?> scoreDefinition,
            List<String> csvLine) {
        return new PickedMoveTypeStepScoreDiffStatisticPoint(Long.parseLong(csvLine.get(0)),
                csvLine.get(1), scoreDefinition.parseScore(csvLine.get(2)));
    }

    // ************************************************************************
    // Write methods
    // ************************************************************************

    @Override
    public void writeGraphFiles(BenchmarkReport benchmarkReport) {
        List<Map<String, XYIntervalSeries>> moveTypeToSeriesMapList = new ArrayList<>(BenchmarkReport.CHARTED_SCORE_LEVEL_SIZE);
        List<PickedMoveTypeStepScoreDiffStatisticPoint> points = getPointList();
        for (PickedMoveTypeStepScoreDiffStatisticPoint point : points) {
            long timeMillisSpent = point.getTimeMillisSpent();
            String moveType = point.getMoveType();
            double[] levelValues = point.getStepScoreDiff().toLevelDoubles();
            for (int i = 0; i < levelValues.length && i < BenchmarkReport.CHARTED_SCORE_LEVEL_SIZE; i++) {
                if (i >= moveTypeToSeriesMapList.size()) {
                    moveTypeToSeriesMapList.add(new LinkedHashMap<>());
                }
                Map<String, XYIntervalSeries> moveTypeToSeriesMap = moveTypeToSeriesMapList.get(i);
                XYIntervalSeries series = moveTypeToSeriesMap.computeIfAbsent(moveType,
                        k -> new XYIntervalSeries(moveType));
                double yValue = levelValues[i];
                // In an XYInterval the yLow must be lower than yHigh
                series.add(timeMillisSpent, timeMillisSpent, timeMillisSpent,
                        yValue, Math.min(yValue, 0.0), Math.max(yValue, 0.0));
            }
        }
        graphFileList = new ArrayList<>(moveTypeToSeriesMapList.size());
        for (int scoreLevelIndex = 0; scoreLevelIndex < moveTypeToSeriesMapList.size(); scoreLevelIndex++) {
            XYPlot plot = createPlot(benchmarkReport, scoreLevelIndex);
            XYItemRenderer renderer = new YIntervalRenderer();
            plot.setRenderer(renderer);
            XYIntervalSeriesCollection seriesCollection = new XYIntervalSeriesCollection();
            for (XYIntervalSeries series : moveTypeToSeriesMapList.get(scoreLevelIndex).values()) {
                seriesCollection.addSeries(series);
            }
            plot.setDataset(seriesCollection);
            String scoreLevelLabel = subSingleBenchmarkResult.getSingleBenchmarkResult().getProblemBenchmarkResult()
                    .findScoreLevelLabel(scoreLevelIndex);
            JFreeChart chart = new JFreeChart(subSingleBenchmarkResult.getName()
                    + " picked move type step " + scoreLevelLabel + " diff statistic",
                    JFreeChart.DEFAULT_TITLE_FONT, plot, true);
            graphFileList.add(writeChartToImageFile(chart,
                    "PickedMoveTypeStepScoreDiffStatisticLevel" + scoreLevelIndex));
        }
    }

    private XYPlot createPlot(BenchmarkReport benchmarkReport, int scoreLevelIndex) {
        Locale locale = benchmarkReport.getLocale();
        NumberAxis xAxis = new NumberAxis("Time spent");
        xAxis.setNumberFormatOverride(new MillisecondsSpentNumberFormat(locale));
        String scoreLevelLabel = subSingleBenchmarkResult.getSingleBenchmarkResult().getProblemBenchmarkResult()
                .findScoreLevelLabel(scoreLevelIndex);
        NumberAxis yAxis = new NumberAxis("Step " + scoreLevelLabel + " diff");
        yAxis.setNumberFormatOverride(NumberFormat.getInstance(locale));
        yAxis.setAutoRangeIncludesZero(true);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        return plot;
    }

}
