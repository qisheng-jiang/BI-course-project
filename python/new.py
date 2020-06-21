import os, io, collections
import pandas as pd
from surprise import Dataset, KNNBaseline, SVD, accuracy, Reader
from surprise.model_selection import cross_validate, train_test_split
import sys
from sys import argv

data = Dataset.load_builtin('ml-100k')