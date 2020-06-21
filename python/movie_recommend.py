import os, io, collections
import pandas as pd
from surprise import Dataset, KNNBaseline, SVD, accuracy, Reader
from surprise.model_selection import cross_validate, train_test_split
import sys
from sys import argv

data = Dataset.load_builtin('ml-100k')

data_df = pd.read_csv('~/.surprise_data/ml-100k/ml-100k/u.data', sep='\t', header=None, names=['user','item','rating','timestamp'])
item_df = pd.read_csv(os.path.expanduser('~/.surprise_data/ml-100k/ml-100k/u.item'), sep='|', encoding='ISO-8859-1', header=None, names=['item','mtitle']+[x for x in range(22)])


data_df = data_df.astype(str)
item_df = item_df.astype(str)

data_df_copy=data_df.copy(deep=False)
item_df_copy=item_df.copy(deep=False)

df=data_df_copy.merge(item_df_copy,on='item',how='left')

item_dict = { item_df.loc[x, 'item']: item_df.loc[x, 'mtitle'] for x in range(len(item_df)) }


item_based_sim_option = {'name': 'pearson_baseline', 'user_based': False}
user_based_sim_option = {'name': 'pearson_baseline', 'user_based': True}

def get_my_ratings(uid):
    myratings_df=df[df['user']==uid][['item','mtitle','rating']]
    return myratings_df

# 为用户推荐n部电影，基于用户的协同过滤算法，先获取10个相似度最高的用户，把这些用户评分高的电影加入推荐列表。
def get_similar_items(iid, n = 10):
    trainset = data.build_full_trainset()
    algo = KNNBaseline(sim_option = item_based_sim_option)
    algo.fit(trainset)
    inner_id = algo.trainset.to_inner_iid(iid)
    # 使用get_neighbors方法得到n个最相似的电影
    neighbors = algo.get_neighbors(inner_id, k=n)
    neighbors_iid = ( algo.trainset.to_raw_iid(x) for x in neighbors )
    recommendations = [ item_dict[x] for x in neighbors_iid ]
    # print('\nten movies most similar to the',item_dict[iid])
    # for i in recommendations:
    #     print(i)
    return recommendations

# 与某电影相似度最高的n部电影，基于物品的协同过滤算法。
def get_similar_users_recommendations(uid, n=10):
    # 获取训练集，这里取数据集全部数据
    trainset = data.build_full_trainset()
    algo = KNNBaseline(sim_option = user_based_sim_option)
    algo.fit(trainset)
    inner_id = algo.trainset.to_inner_uid(uid)
    # 使用get_neighbors方法得到10个最相似的用户
    neighbors = algo.get_neighbors(inner_id, k=10)
    neighbors_uid = ( algo.trainset.to_raw_uid(x) for x in neighbors )
    recommendations = set()
    #把评分为5的电影加入推荐列表
    for user in neighbors_uid:
        if len(recommendations) > n:
            break
        item = data_df[data_df['user']==user]
        item = item[item['rating']=='5']['item']
        for i in item:
            print(item_dict[i])
            recommendations.add(item_dict[i])
    # print('\nrecommendations for user',uid)
    # for i, j in enumerate(list(recommendations)):
    #     if i >= 10:
    #         break
    #     print(j)
    return recommendations

#电影推荐示例

num1 = argv[1]
num2 = argv[2]

get_similar_users_recommendations(num1, int(num1))
#
# Estimating biases using als...
# Computing the msd similarity matrix...
# Done computing similarity matrix.
#
# recommendations for user 1
# Hoop Dreams (1994)
# Mrs. Brown (Her Majesty, Mrs. Brown) (1997)
# Liar Liar (1997)
# Scream (1996)
# Contact (1997)
# Saint, The (1997)
# Antonia's Line (1995)
# It Happened One Night (1934)
# Full Monty, The (1997)
# Game, The (1997)

#基于评分的电影相似
# get_similar_items('2',10)
#
# Estimating biases using als...
# Computing the msd similarity matrix...
# Done computing similarity matrix.
#
# ten movies most similar to the GoldenEye (1995)
# Evil Dead II (1987)
# Hoop Dreams (1994)
# Speed (1994)
# Grand Day Out, A (1992)
# Ed Wood (1994)
# Adventures of Pinocchio, The (1996)
# Highlander (1986)
# Unforgiven (1992)
# Down Periscope (1996)
# Bullets Over Broadway (1994)