使用xsimilarity过程中有什么问题或建议，欢迎大家在此处留言！

# 问题列表 #

  * 我从svn上检出了代码，有一项测试通不过：src/test中ruc.irm.similarity.statistic.DictStatisticTest 无法读取文件:./db/coredict.xml.gz（感谢wubo.wb提出的问题）
解决办法：./db/coredict.xml.gz是利用的ictclas4j的词典文件，这个文件可以从lib/ictclas4j.jar文件中得到， 即：把ictclas4j.jar文件解压开，里面的dictionary目录下有coredict.xml.gz文件。