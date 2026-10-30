import { useState } from "react";
import {
    creatureRows,
    formatValue,
    sortRows,
    type Picked,
    type Row,
    type SortKey,
} from "./inspect";
import type { WorldSnapshot } from "./types";

type Props = { snapshot: WorldSnapshot; selected: number | null; onPick(picked: Picked): void };

/** Rows are taken when it mounts and when Refresh is pressed, so they hold still to be clicked. */
export function CreatureTable({ snapshot, selected, onPick }: Props) {
    const [rows, setRows] = useState<Row[]>(() => creatureRows(snapshot));
    const [by, setBy] = useState<SortKey>("age");
    return (
        <section className="creatures">
            <div className="creatures-head">
                <button onClick={() => setRows(creatureRows(snapshot))}>Refresh</button>
                <label>
                    Sort by{" "}
                    <select
                        value={by}
                        onChange={(e) => setBy(e.target.value === "reserve" ? "reserve" : "age")}
                    >
                        <option value="age">age</option>
                        <option value="reserve">reserve</option>
                    </select>
                </label>
                <span>{rows.length} creatures</span>
            </div>
            <div className="creatures-scroll">
                <table>
                    <thead>
                        <tr>
                            <th>Species</th>
                            <th>Id</th>
                            <th>Age</th>
                            <th>Reserve</th>
                            <th>Days to starve</th>
                        </tr>
                    </thead>
                    <tbody>
                        {sortRows(rows, by).map((row) => (
                            <tr
                                key={row.id}
                                className={row.id === selected ? "selected" : undefined}
                                onClick={() => onPick({ id: row.id, species: row.species })}
                            >
                                <td>{row.species}</td>
                                <td>{row.id}</td>
                                <td>{formatValue(row.age)}</td>
                                <td>{formatValue(row.reserve)}</td>
                                <td>{formatValue(row.daysToStarve)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
