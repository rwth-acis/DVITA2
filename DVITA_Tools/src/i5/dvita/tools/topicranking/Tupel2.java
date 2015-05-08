package i5.dvita.tools.topicranking;

public class Tupel2 implements Comparable<Tupel2>{
	public Double value;
	public Integer ID;

    public Tupel2(Double SimilarityValue1, Integer id) {
        this.value = SimilarityValue1;
        this.ID = id;
    }

    @Override
    public int compareTo(Tupel2 o) {
        return value < o.value ? -1 :value > o.value ? 1 : 0;
    }

}
