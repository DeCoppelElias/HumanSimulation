import { useCallback, useState } from "react";
import { addressFor, worldFromAddress } from "./address";
import type { Api } from "./api";
import { Notice } from "./Notice";
import { Picker } from "./Picker";
import { WorldView } from "./WorldView";

export function App({ api }: { api: Api }) {
    const [worldId, setWorldId] = useState(() => worldFromAddress(window.location.search));
    const [notice, setNotice] = useState<string | null>(null);

    const open = useCallback((id: string | null) => {
        window.history.replaceState(null, "", addressFor(id));
        setWorldId(id);
    }, []);
    const leave = useCallback(
        (reason: string | null) => {
            if (reason !== null) {
                setNotice(reason);
            }
            open(null);
        },
        [open],
    );

    return (
        <main>
            {notice !== null && <Notice text={notice} onClose={() => setNotice(null)} />}
            {worldId === null ? (
                <Picker api={api} onOpen={open} onError={setNotice} />
            ) : (
                <WorldView
                    key={worldId}
                    api={api}
                    id={worldId}
                    onLeave={leave}
                    onError={setNotice}
                />
            )}
            <footer>
                Rabbit sprite by <a href="https://kenney.nl">Kenney</a>, CC0. Wolf sprite from{" "}
                <a href="https://github.com/jdecked/twemoji">Twemoji</a> by Twitter, Inc and other
                contributors, <a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>.
            </footer>
        </main>
    );
}
