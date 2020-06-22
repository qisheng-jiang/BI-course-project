# -*- coding: UTF-8 -*-
import sys

import pandas as pd
import json
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sys import argv
import os
import sys


def predictGenreAmount(param, year):
    # param = 'Action' year = 2019
    path = os.path.abspath(os.path.dirname(sys.argv[0]))
    df = pd.read_csv(path + "/tmdb_5000_movies.csv", encoding='utf-8')
    df['release_date'] = df['release_date'].astype('datetime64')  # 时间字段转为时间类型
    df['release_year'] = df['release_date'].dt.year
    df = df[df['release_year'] < 2014]
    df = df[df['release_year'] > 1980]
    df['genres_new'] = list(map(lambda x: function(x), df['genres']))
    data = df.groupby(['genres_new', 'release_year']
                      ).size().unstack('genres_new').fillna(0)
    # data.plot()

    model = RandomForestRegressor(
        n_estimators=20, random_state=50, n_jobs=-1, oob_score=True)
    amount_y = data[param].values
    year_x = data[param].index.values.reshape(-1, 1)

    model.fit(year_x, amount_y)
    return model.predict(np.array([[year]]))[0]


def function(x):
    listify = json.loads(x)
    if len(listify) > 0:
        return json.loads(x)[0]['name']
    return 'null'


num1 = argv[1]
num2 = argv[2]
print(predictGenreAmount(num1, int(num2)))

