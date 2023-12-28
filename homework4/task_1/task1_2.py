from pyspark.sql import SparkSession
from pyspark.sql.functions import col

# 初始化Spark会话
spark = SparkSession.builder.appName("LoanIncomeDifference").getOrCreate()

#读取 CSV 文件
df = spark.read.csv("file:///home/Lsc/application_data.csv", header=True, inferSchema=True)

# 计算差值
df = df.withColumn("Difference", col("AMT_CREDIT") - col("AMT_INCOME_TOTAL"))

# 获取差值最高的十条记录
top_10_max_diff = df.orderBy(col("Difference").desc()).limit(10)

# 获取差值最低的十条记录
top_10_min_diff = df.orderBy(col("Difference")).limit(10)

# 输出结果
top_10_max_diff.select("SK_ID_CURR", "NAME_CONTRACT_TYPE", "AMT_CREDIT", "AMT_INCOME_TOTAL", "Difference").show()
top_10_min_diff.select("SK_ID_CURR", "NAME_CONTRACT_TYPE", "AMT_CREDIT", "AMT_INCOME_TOTAL", "Difference").show()

# 关闭Spark会话
spark.stop()
