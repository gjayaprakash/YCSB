java -Xms2g -Xmx2g -cp ../db/terracotta-3.5/lib/*:../db/terracotta-3.5/conf:../build/ycsb.jar -Dcom.tc.productkey.path=../terracotta-license.key com.yahoo.ycsb.Client -t -db com.yahoo.ycsb.db.TerracottaClient -P ../workloads/workloadd -P ../properties/$1.dat -s > ../tc/d.out 2> ../tc/d.stats
