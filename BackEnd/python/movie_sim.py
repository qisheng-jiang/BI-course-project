import pandas as pd
import numpy as np
from scipy import spatial
import os, sys
from sys import argv

path = os.path.abspath(os.path.dirname(sys.argv[0]))
movies=pd.read_csv(path + '\movies_graph_sim.csv')

# df=pd.read_csv(path + '\movieInfo.csv', usecols=['name','director','star'])
# df['star']=df['star'].str.split('|', expand=False)
# movies=df.dropna().copy()

# def countN(column):
#     count=dict()
#     for row in column:
#         for ele in row:
#             if ele in count:
#                 count[ele]+=1
#             else:
#                 count[ele]=1
#     return count

# def binary(wordlist0, wordlist):
#     binary = []
#     for word in wordlist0.index:
#         if word in wordlist:
#             binary.append(1)
#         else:
#             binary.append(0)
#     return binary

# directors=movies.groupby('director').size().sort_values(ascending=False)
# actors=pd.Series(countN(movies.star)).sort_values(ascending=False)

# movies['director_bin']=[binary(directors,x) for x in movies.director]
# movies['actors_bin']=[binary(actors, x) for x in movies.star]

#参数为电影名字
def angle(name1,name2):
    dis_tot=0
    movie1=movies[movies['name']==name1]
    movie2=movies[movies['name']==name2]
    iterlist=[[movie1.director_bin, movie2.director_bin],
              [movie1.actors_bin, movie2.actors_bin]]
    for b1, b2 in iterlist:
        # print(b1,b2)
        if(1 not in b1.values[0]) or (1 not in b2.values[0]):
            dis=1
            # print(0)
        else:
            dis=spatial.distance.cosine(b1.values[0], b2.values[0])
            # print(1)
        dis_tot+=dis
    return dis_tot

#示例（值越小越相似）
num1 = argv[1]
num2 = argv[2]
print(angle(num1,num2))
#angle('Manhattan (film)','Manhattan Murder Mystery')
#0.7418011102528388