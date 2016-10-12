
public class Driver {
	public static void main(String[] args) throws Exception {

        /*
            args0: original dataset
            args1: output directory for DividerByUser job
            args2: output directory for coOccurrenceMatrixBuilder job
            args3: output directory for NormalizeCoOccurrenceMatrix job
            args4: output directory for MultiplicationMapperJoin job
            args5: output directory for MultiplicationSum job
            args6: output directory for RecommenderListGenerator job
        */

		DataDividerByUser dataDividerByUser = new DataDividerByUser();
		CoOccurrenceMatrixGenerator coOccurrenceMatrixGenerator = new CoOccurrenceMatrixGenerator();
		NormalizeCoOccurrenceMatrix normalizeCoOccurrenceMatrix = new NormalizeCoOccurrenceMatrix();
		MultiplicationMapperJoin mapperJoin = new MultiplicationMapperJoin();
		MultiplicationSum multiplicationSum = new MultiplicationSum();
		RecommenderListGenerator generator = new RecommenderListGenerator();

		String[] path1 = {args[0], args[1]};
		String[] path2 = {args[1], args[2]};
		String[] path3 = {args[2], args[3]};
		String[] path4 = {args[3], args[0], args[4]};
		String[] path5 = {args[4], args[5]};
		String[] path6 = {args[5], args[6]};

		dataDividerByUser.main(path1);
		coOccurrenceMatrixGenerator.main(path2);
		normalizeCoOccurrenceMatrix.main(path3);
		mapperJoin.main(path4);
		multiplicationSum.main(path5);
		generator.main(path6);
	}

}
