package src;



public class Main
{

  public static int[][] whatisthematrix_user_item(){
    int user_item[][]={{0,0,0,1,1,1,0,1,0,1,0,0,0,0,0},{0,0,0,0,0,0,0,1,0,1,1,1,0,0,0},{0,0,1,0,0,0,1,1,1,0,0,0,0,0,0},{1,1,1,0,0,0,1,0,0,0,0,0,1,1,1},{0,0,0,0,0,0,0,0,0,0,0,0,1,0,1},{0,0,1,0,0,0,0,0,0,1,1,0,0,0,0}};
    return user_item;
  }
  
  public static int[][] whatisthematrix_item_tag(){
    int item_tag[][]={{1,1,1,1,0,0,0,0,0,0,0,0,0,0},{1,1,1,1,0,0,0,0,0,0,0,0,0,0},{0,1,1,1,1,1,0,0,0,0,0,0,0,0},{0,1,0,1,1,0,1,0,0,0,0,0,0,0},{0,0,1,1,0,0,1,1,1,0,0,0,0,0},{0,0,1,1,0,0,1,1,1,0,0,0,0,0},{0,0,1,1,0,1,0,0,0,1,0,0,0,0},{0,0,1,1,0,1,1,0,0,1,0,0,0,0},{0,0,1,1,0,0,0,0,0,0,1,0,0,0},{0,0,0,1,0,0,1,0,1,0,0,1,0,0},{0,0,0,1,0,0,0,0,1,0,0,0,0,0},{0,0,1,0,0,0,0,0,1,0,0,0,1,0},{0,1,0,1,0,1,0,0,0,0,0,0,0,1},{0,1,0,0,0,0,0,0,0,0,0,0,1,0},{0,1,0,1,0,0,0,0,0,0,0,0,0,1}};
    return item_tag;
  }
  
  public static int[] get_user_item_array(int u_number){
    int sum=0;
    int user_item_matrix[][]=whatisthematrix_user_item();
    for(int i=0; i<user_item_matrix[u_number].length; i++){
      if(user_item_matrix[u_number][i]!=0){
        sum++;
      }
    }
    int item_array[]=new int[sum];
    int seg=0;  // item_array-be sorba rakjuk be az elemeket.
    for(int i=0; i<user_item_matrix[u_number].length; i++){
      if(user_item_matrix[u_number][i]!=0){
        item_array[seg]=i;
        seg++;
      }
    }
    return item_array;
  }
  
  public static int[] get_item_tag_array(int i_number){
    int sum=0;
    int item_tag_matrix[][]=whatisthematrix_item_tag();
    for(int i=0; i<item_tag_matrix[i_number].length; i++){
      if(item_tag_matrix[i_number][i]!=0){
        sum++;
      }
    }
    int tag_array[]=new int[sum];
    int seg=0;
    for(int i=0; i<item_tag_matrix[i_number].length; i++){
      if(item_tag_matrix[i_number][i]!=0){
        tag_array[seg]=i;
        seg++;
      }
    }
    return tag_array;
  }
  
  
  public static int[] m_matrix_sora(int u_number, int tag_number){ 
    int m_matrix_sor[]= new int [tag_number]; // visszatérési érték, egy felhasználó hányszor érintkezett egy tag-gel.
    int [] item_array=get_user_item_array(u_number); // lekérem az adott felhasználóhoz tartozó item tömböt.
    for(int i=0; i<item_array.length; i++){
        int tag_array[]=get_item_tag_array(item_array[i]); // lekérem az adott itemhez tartozó tag tömböt.
          for(int k=0; k<tag_array.length; k++){
            m_matrix_sor[tag_array[k]]++;     // növelem az u_number user mely taggel hányszor lépett kapcsolatba.
          }
    }
    return m_matrix_sor;
  }
  
  
  public static double[][] sulyozas(int sum_user_num, int sum_tag_num, int sum_item_num){ // felhaszn. száma; tagek száma;
    
    int sum=0; 
    double temp_tagscore=0;
    double tagscore[]=new double[sum_tag_num]; // minden tagjéhez rakunk score-t, ez is biztos szar lesz késõbb.
    double tag_weight=0.0;
    double user_item_weights[][]= new double[sum_user_num][sum_item_num];
    
    //-------------------------------------------------------------------------------------------------------
    //--------user-tag mátrix azt jelzi, hogy egy user adott taggel hányszor lépett kapcsolatba--------------
    //--------item tag mátrix, 0;1 értékek, itemben van e ilyen tag, vagy sem--------------------------------
    //-------------------------------------------------------------------------------------------------------
    
    for(int i=0; i<sum_user_num; i++){  // usereken végigfutó ciklus
     int user_tag_array[]=m_matrix_sora(i,sum_tag_num); // i. user egyes tagekkel hányszor lépett kapcsolatba
     sum=0; 
      for(int j=0; j<sum_tag_num; j++){
          sum=sum+user_tag_array[j];  // minden tag elõfordulásának a száma.
      }
      
      for(int j=0; j<sum_tag_num; j++){  // sajnos megint végig kell menni, a konkrét tagscore kiszámolásához.
          if(user_tag_array[j]!=0 && sum!=0){
              temp_tagscore=(double)user_tag_array[j]/(double)sum;
          }
          else{
              temp_tagscore=0;
          }
          tagscore[j]=temp_tagscore;      // feltöltjük a tagekhez tartozó tagscore tömböt.
      }
      for(int k=0; k<sum_item_num; k++){
        tag_weight=0;
        int tag_array[]=get_item_tag_array(k); // lekérem az adott itemhez tartozó tag tömböt.
        for(int j=0; j<tag_array.length; j++){
             tag_weight=tag_weight+tagscore[tag_array[j]]; // lekért tömb elemei== tagid hoz tartozó tagscoreok összeadása.
        }
        user_item_weights[i][k]=tag_weight; // i. felhasználó k. eleméhez tartozó tag súlyozás
      }
    }
    return user_item_weights;
  }
  
  
  public static void main(String[] args)
  {
    double [][] suly_fuggveny=sulyozas(6,14,15);
    int user_item_matrix[][]=whatisthematrix_user_item();
    
    double ials[][]={{0.7721551367319163, 0.7721551367319163, 0.7721551367319163, 0.8488801759253776, 0.8696296373635786, 0.8269908272773283, 0.7807767287171629, 0.5617796760803059, 0.37321446423561333, 0.5289488667193041, 0.048834764584015505, 0.048834764584015505, -0.07156568948235793, 0.048834764584015505, -0.07156568948235793},{0.7033573380525698, 0.7033573380525698, 0.7033573380525698, 0.8467638780108779, 0.8424699081522941, 0.8169986377176354, 0.7443241976233739, 0.6600277240723523, 0.49562309873907007, 0.5995748553453282, 0.19351137291102055, 0.19351137291102055, 0.10089588633006244, 0.19351137291102055, 0.10089588633006244},{0.4756390738599591, 0.4756390738599591, 0.4756390738599591, 0.7838639502289415, 0.7143119217587701, 0.7355005992682376, 0.5984916325684994, 0.8724721866938537, 0.7824426795153289, 0.7438161964993912, 0.559080756787695, 0.559080756787695, 0.5454638062366985, 0.559080756787695, 0.5454638062366985},{0.05120095627730104, 0.05120095627730104, 0.05120095627730104, 0.44137101611550217, 0.32125394352887543, 0.3884502743851316, 0.22521966379539768, 0.8140503408701235, 0.8400967203604216, 0.6518680333865075, 0.7838400633163849, 0.7838400633163849, 0.8652028771603835, 0.7838400633163849, 0.8652028771603835},{-0.1612341846345916, -0.1612341846345916, -0.1612341846345916, 0.15980740157276124, 0.0491319486304537, 0.11932708628368813, -0.011216341236301178, 0.562626559650479, 0.6357437624432947, 0.4294293674907719, 0.6730633726613854, 0.6730633726613854, 0.77640920081587, 0.6730633726613854, 0.77640920081587},{0.6109424711467458, 0.6109424711467458, 0.6109424711467458, 0.8316929053575662, 0.7976165117338754, 0.7929825115940968, 0.689850395242357, 0.767335548060309, 0.6341612676641739, 0.6748593797127874, 0.3630651086958482, 0.3630651086958482, 0.30493550566957967, 0.3630651086958482, 0.30493550566957967}};
    double szorzat[][]= new double [6][15];
    
    for(int i=0; i<6; i++){
      for(int j=0; j<15; j++){
        if(user_item_matrix[i][j]==0){
          szorzat[i][j]=ials[i][j]*suly_fuggveny[i][j];
        }
        else{
          szorzat[i][j]=ials[i][j];
        }
      }
    }
    
    for(int i=0; i<6; i++){
      for(int j=0; j<15; j++){
        System.out.print(" " + szorzat[i][j]);
      }
      System.out.println("");
    }
    System.out.println("");
    System.out.println("");
  
    
    for(int i=0; i<6; i++){
      for(int j=0; j<15; j++){
        System.out.print(" " + ials[i][j]);
      }
      System.out.println("");
    }
    
  }
}
