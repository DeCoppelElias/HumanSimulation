import { useEffect, useMemo, useRef } from "react";
import uPlot from "uplot";
import "uplot/dist/uPlot.min.css";
import { speciesIn, toSeries } from "./census";
import type { CensusRow } from "./types";

const COLOURS = ["#8d6e63", "#5c6bc0", "#26a69a", "#ef5350"];

type Props = { history: CensusRow[] };

export function Chart({ history }: Props) {
    const host = useRef<HTMLDivElement>(null);
    const plot = useRef<uPlot | null>(null);
    // Joined to a string so the chart is rebuilt when the species change, not on every day.
    const speciesKey = useMemo(() => speciesIn(history).join(","), [history]);

    useEffect(() => {
        const element = host.current;
        if (element === null) {
            return;
        }
        const species = speciesKey === "" ? [] : speciesKey.split(",");
        const made = new uPlot(
            {
                width: 480,
                height: 240,
                scales: { x: { time: false } },
                series: [
                    { label: "day" },
                    ...species.map((name, index) => ({
                        label: name,
                        stroke: COLOURS[index % COLOURS.length],
                        width: 2,
                    })),
                ],
            },
            [[], ...species.map(() => [])] as uPlot.AlignedData,
            element,
        );
        plot.current = made;
        return () => {
            made.destroy();
            plot.current = null;
        };
    }, [speciesKey]);

    useEffect(() => {
        const species = speciesKey === "" ? [] : speciesKey.split(",");
        plot.current?.setData(toSeries(history, species) as uPlot.AlignedData);
    }, [history, speciesKey]);

    return <div ref={host} className="chart" />;
}
