/* eslint-disable @typescript-eslint/no-unused-vars */
/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import CircularProgress from '@material-ui/core/CircularProgress';
import { makeStyles } from '@material-ui/core/styles';
import React, { useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import Page from '../components/Page';
import ReaderNavBar, { defaultReaderSettings, IReaderSettings } from '../components/ReaderNavBar';
import NavbarContext from '../context/NavbarContext';
import client from '../util/client';
import useLocalStorage from '../util/useLocalStorage';

const useStyles = (settings: IReaderSettings) => makeStyles({
    reader: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        margin: '0 auto',
    },

    loading: {
        margin: '50px auto',
    },

    pageNumber: {
        display: settings.showPageNumber ? 'block' : 'none',
        position: 'fixed',
        bottom: '50px',
        right: settings.staticNav ? 'calc((100vw - 325px)/2)' : 'calc((100vw - 25px)/2)',
        width: '50px',
        textAlign: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.3)',
        borderRadius: '10px',
    },
});

const range = (n:number) => Array.from({ length: n }, (value, key) => key);
const initialChapter = () => ({ pageCount: -1, chapterIndex: -1, chapterCount: 0 });

export default function Reader() {
    const [settings, setSettings] = useLocalStorage<IReaderSettings>('readerSettings', defaultReaderSettings);

    const classes = useStyles(settings)();

    const [serverAddress] = useLocalStorage<String>('serverBaseURL', '');

    const { chapterIndex, mangaId } = useParams<{chapterIndex: string, mangaId: string}>();
    const [manga, setManga] = useState<IMangaCard | IManga>({ id: +mangaId, title: '', thumbnailUrl: '' });
    const [chapter, setChapter] = useState<IChapter | IPartialChpter>(initialChapter());
    const [curPage, setCurPage] = useState<number>(0);

    const { setOverride, setTitle } = useContext(NavbarContext);
    useEffect(() => {
        setOverride(
            {
                status: true,
                value: (
                    <ReaderNavBar
                        settings={settings}
                        setSettings={setSettings}
                        manga={manga}
                        chapter={chapter}
                        curPage={curPage}
                    />
                ),
            },
        );

        // clean up for when we leave the reader
        return () => setOverride({ status: false, value: <div /> });
    }, [manga, chapter, settings, curPage, chapterIndex]);

    useEffect(() => {
        setTitle('Reader');
        client.get(`/api/v1/manga/${mangaId}/`)
            .then((response) => response.data)
            .then((data: IManga) => {
                setManga(data);
                setTitle(data.title);
            });
    }, [chapterIndex]);

    useEffect(() => {
        setChapter(initialChapter);
        client.get(`/api/v1/manga/${mangaId}/chapter/${chapterIndex}`)
            .then((response) => response.data)
            .then((data:IChapter) => {
                setChapter(data);
            });
    }, [chapterIndex]);

    if (chapter.pageCount === -1) {
        return (
            <div className={classes.loading}>
                <CircularProgress thickness={5} />
            </div>
        );
    }
    return (
        <div className={classes.reader}>
            <div className={classes.pageNumber}>
                {`${curPage + 1} / ${chapter.pageCount}`}
            </div>
            {range(chapter.pageCount).map((index) => (
                <Page
                    key={index}
                    index={index}
                    src={`${serverAddress}/api/v1/manga/${mangaId}/chapter/${chapterIndex}/page/${index}`}
                    setCurPage={setCurPage}
                    settings={settings}
                />
            ))}
        </div>
    );
}
