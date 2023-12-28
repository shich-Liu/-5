from pyspark.sql import SparkSession
from pyspark.sql.functions import col, count, format_number

# 初始化Spark会话
spark = SparkSession.builder.appName("GenderChildrenAnalysis").getOrCreate()
#读取 CSV 文件
df = spark.read.csv("file:///home/Lsc/application_data.csv", header=True, inferSchema=True)
# 过滤出男性客户
men_df = df.filter(df["CODE_GENDER"] == "M")
# 计算男性客户总数
total_men_count = men_df.count()
# 对 CNT_CHILDREN 进行分组并计算每组的数量
result = men_df.groupBy("CNT_CHILDREN").count()
# 计算每种小孩个数的占比并格式化为保留8位小数
result = result.withColumn("formatted_ratio", format_number((col("count") / total_men_count), 8))
# 选择需要的列
result_df = result.select("CNT_CHILDREN", "count", "formatted_ratio")
# 按照 CNT_CHILDREN 升序排列
ordered_result_df = result_df.orderBy("CNT_CHILDREN")
# 显示结果
ordered_result_df.show()
# 关闭 SparkSession
spark.stop()
