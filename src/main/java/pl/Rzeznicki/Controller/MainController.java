package pl.Rzeznicki.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import pl.Rzeznicki.Entity.*;
import pl.Rzeznicki.Repo.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class MainController {

    private final CategoriesRepo categoriesRepo;
    private final ImagesRepo imagesRepo;
    private final RecipesRepo recipesRepo;
    private final UsersRepo usersRepo;
    private final RatingsRepo ratingsRepo;
    private final CommentariesRepo commentariesRepo;
    public static String uploadDirectory = System.getProperty("user.dir")+"/src/main/resources/static/";

    @Autowired
    public MainController(CategoriesRepo categoriesRepo, ImagesRepo imagesRepo, RecipesRepo recipesRepo, UsersRepo usersRepo, RatingsRepo ratingsRepo, CommentariesRepo commentariesRepo) {
        this.categoriesRepo = categoriesRepo;
        this.imagesRepo = imagesRepo;
        this.recipesRepo = recipesRepo;
        this.usersRepo=usersRepo;
        this.ratingsRepo = ratingsRepo;
        this.commentariesRepo = commentariesRepo;
    }

    @GetMapping("/")
    public String showIndex(Users users, Model model,HttpServletRequest request) {
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("session", request.getSession().getAttribute("isLogin"));
        model.addAttribute("recipes", recipesRepo.findAll());
        return "index";
    }

    @PostMapping("/adduser")
    public RedirectView addUser(@Valid Users users, Model model, HttpServletRequest request){
        users.setRole("Użytkownik");
        usersRepo.save(users);
        request.getSession().setAttribute("isLogin", true);
        request.getSession().setAttribute("login", users.getLogin());
        request.getSession().setAttribute("id", users.getId());
        request.getSession().setAttribute("nickname", users.getNickname());
        model.addAttribute("session", request.getSession().getAttribute("isLogin"));
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("recipes", recipesRepo.findAll());
        return new RedirectView("/");
    }
    @PostMapping("/loginuser")
    public RedirectView loginUser(@Valid Users users, Model model, HttpServletRequest request){
        Users userDl=usersRepo.findByLogin(users.getLogin());
        request.getSession().setAttribute("login", users.getLogin());
        if(userDl != null && userDl.getPassword().equals(users.getPassword()))
        {
            request.getSession().setAttribute("isLogin", true);
            request.getSession().setAttribute("id", userDl.getId());
            request.getSession().setAttribute("nickname", userDl.getNickname());
            request.getSession().setAttribute("role", userDl.getRole());
            model.addAttribute("session", request.getSession().getAttribute("isLogin"));
            model.addAttribute("categories", categoriesRepo.findAll());
            model.addAttribute("recipes", recipesRepo.findAll());
        } else {
            request.getSession().setAttribute("isLogin", false);
        }
        return new RedirectView("/");
    }

    @PostMapping("/logout")
    public RedirectView logoutUser(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        if(session.getAttribute("isLogin") != null){
            session.removeAttribute("id");
            session.removeAttribute("nickname");
            session.removeAttribute("role");
            session.invalidate();
        }
        return new RedirectView("/");
    }

    @GetMapping("/recipes")
    public String recipesPage(@Valid Users users, Model model, Recipes recipes){
        List<Ratings> ratingsList= (List<Ratings>) ratingsRepo.findAll();
        List<Recipes> recipesList= (List<Recipes>) recipesRepo.findAll();
        Map<Long, Integer> avgs = new HashMap<Long, Integer>();
        for(Recipes eachrecipe:recipesList) {
            int sum=0;
            int counter=0;
            int avg=0;
            for (Ratings ratings : ratingsList) {
                if (ratings.getRecipes().getId() == eachrecipe.getId()) {
                    sum += ratings.getRate();
                    counter++;
                }
            }
            if(counter!=0) {
                avg = (sum/counter);
            }

            avgs.put(eachrecipe.getId(), avg);
        }

        model.addAttribute("recipes1", recipesRepo.findAll());
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("images", imagesRepo.findAll());
        model.addAttribute("users1", usersRepo.findAll());
        model.addAttribute("avgs", avgs);
        return "recipes";
    }

    @GetMapping(value = "/deleteComment/{commentarieId}/{recipeId}")
    public RedirectView recipePage(Model model, @PathVariable("commentarieId") long commentarieId,@PathVariable("recipeId") long recipeId){
        Commentaries commentaries=commentariesRepo.findById(commentarieId).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + commentarieId));
        commentariesRepo.delete(commentaries);
        return new RedirectView("/recipe/"+recipeId);
    }

    @GetMapping(value = "/recipe/{recipeId}")
    public String recipePage(@Valid Users users,Commentaries commentaries, Model model, @PathVariable("recipeId") long recipeId, HttpServletRequest request){
        HttpSession session = request.getSession();
        Recipes recipe  = recipesRepo.findById(recipeId).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + recipeId));
        long idC=recipe.getCategories().getId();
        Categories categories  = categoriesRepo.findById(idC).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + idC));
        Images images= imagesRepo.findByRecipesId(recipeId);
        List<Commentaries> commentariesList = (List<Commentaries>) commentariesRepo.findAll();
        List<Commentaries> commentariesFinalList=new ArrayList<>();
        for(Commentaries commentarie:commentariesList){
            if(commentarie.getRecipes().getId()==recipeId){
                commentariesFinalList.add(commentarie);
            }
        }
        long idU = recipe.getUser().getId();
        Users user = usersRepo.findById(idU).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + idU));
        List<Ratings> ratingsList = (List<Ratings>) ratingsRepo.findAll();
        Ratings ratings2=null;
        Long session_uid = (Long) session.getAttribute("id");
        if(session_uid!=null) {
            for (Ratings ratings : ratingsList) {
                System.out.println();
                if ((ratings.getUsers().getId() == (long) session_uid) && (ratings.getRecipes().getId() == recipeId)) {
                    ratings2 = ratings;
                }
            }
        }

        int sum=0;
        int counter=0;
        int avg=0;

        for(Ratings ratings:ratingsList){
            if(ratings.getRecipes().getId()==recipeId){
                sum+=ratings.getRate();
                counter++;
            }
        }

        if(counter!=0) {
            avg = (sum/counter);
        }

        if(ratings2 != null) {
            if (ratings2.getUsers().getId() == session_uid && ratings2.getRecipes().getId() == recipeId) {
                Ratings ratings3 = ratingsRepo.findByUsersId(session_uid);
                ratings3.setRate(0);
                model.addAttribute("ratings", ratings3);
            } else {
                model.addAttribute("ratings", ratings2);
            }
        } else {
            Ratings ratings3 = new Ratings();
            ratings3.setRate(0);
            model.addAttribute("ratings", ratings3);
        }
        int current=0;
        boolean moreThan5=false;
        for (Commentaries commentaries1 : commentariesList) {
            if(commentaries1.getRecipes().getId()==recipeId){
                current++;
            }
        }
        if(current >= 5) {
            moreThan5=true;
        }
        model.addAttribute("isMoreThan5",moreThan5);
        model.addAttribute("recipes", recipe);
        model.addAttribute("categories", categories);
        model.addAttribute("images", images);
        model.addAttribute("commentaries1", commentariesFinalList);
        model.addAttribute("users", user);
        model.addAttribute("rate", avg);
        model.addAttribute("users2", usersRepo.findAll());
        model.addAttribute("isLogin",session.getAttribute("isLogin"));
        model.addAttribute("loggedUserId",session.getAttribute("id"));
        return "recipe";
    }

    @PostMapping(value= "/recipes/sortCategory")
    public RedirectView sortCategory(Model model){

        return new RedirectView("/recipesCategory");
    }
    @GetMapping("/recipesCategory")
    public String recipesPageSortCategory(@Valid Users users, Model model, Recipes recipes){
        List<Ratings> ratingsList= (List<Ratings>) ratingsRepo.findAll();
        List<Recipes> recipesList= (List<Recipes>) recipesRepo.findByOrderByCategoriesNameDesc();
        Map<Long, Integer> avgs = new HashMap<Long, Integer>();
        for(Recipes eachrecipe:recipesList) {
            int sum=0;
            int counter=0;
            int avg=0;
            for (Ratings ratings : ratingsList) {
                if (ratings.getRecipes().getId() == eachrecipe.getId()) {
                    sum += ratings.getRate();
                    counter++;
                }
            }
            if(counter!=0) {
                avg = (sum/counter);
            }

            avgs.put(eachrecipe.getId(), avg);
        }

        model.addAttribute("recipes1", recipesList);
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("images", imagesRepo.findAll());
        model.addAttribute("users1", usersRepo.findAll());
        model.addAttribute("avgs", avgs);
        return "recipes";
    }

    @PostMapping(value= "/recipes/sortAuthor")
    public RedirectView sortAuthor(Model model){

        return new RedirectView("/recipesAuthor");
    }
    @GetMapping("/recipesAuthor")
    public String recipesPageSortAuthor(@Valid Users users, Model model, Recipes recipes){
        List<Ratings> ratingsList= (List<Ratings>) ratingsRepo.findAll();
        List<Recipes> recipesList= (List<Recipes>) recipesRepo.findByOrderByUsersNicknameDesc();
        Map<Long, Integer> avgs = new HashMap<Long, Integer>();
        for(Recipes eachrecipe:recipesList) {
            int sum=0;
            int counter=0;
            int avg=0;
            for (Ratings ratings : ratingsList) {
                if (ratings.getRecipes().getId() == eachrecipe.getId()) {
                    sum += ratings.getRate();
                    counter++;
                }
            }
            if(counter!=0) {
                avg = (sum/counter);
            }

            avgs.put(eachrecipe.getId(), avg);
        }

        model.addAttribute("recipes1", recipesList);
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("images", imagesRepo.findAll());
        model.addAttribute("users1", usersRepo.findAll());
        model.addAttribute("avgs", avgs);
        return "recipes";
    }

    @PostMapping(value= "/recipes/sortLevel")
    public RedirectView sortPost(Model model){

        return new RedirectView("/recipesLevel");
    }
    @GetMapping("/recipesLevel")
    public String recipesPageSortLevel(@Valid Users users, Model model, Recipes recipes){
        List<Ratings> ratingsList= (List<Ratings>) ratingsRepo.findAll();
        List<Recipes> recipesList= (List<Recipes>) recipesRepo.findAll();
        List<Integer> avgs = new ArrayList<>();
        Map<Long, Integer> avgs1 = new HashMap<Long, Integer>();
        for(Recipes eachrecipe:recipesList) {
            int sum=0;
            int counter=0;
            int avg=0;
            for (Ratings ratings : ratingsList) {
                if (ratings.getRecipes().getId() == eachrecipe.getId()) {
                    sum += ratings.getRate();
                    counter++;
                }
            }
            if(counter!=0) {
                avg = (sum/counter);
            }

            avgs.add(avg);
            avgs1.put(eachrecipe.getId(), avg);
        }

        sort(recipesList,avgs,0,recipesList.size()-1);

        model.addAttribute("recipes1", recipesList);
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("images", imagesRepo.findAll());
        model.addAttribute("users1", usersRepo.findAll());
        model.addAttribute("avgs", avgs1);
        return "recipes";
    }

    @PostMapping(value = "/recipe/addCommentarie/{recipeId2}")
    public RedirectView addCommentarie(@Valid Users users, @Valid Commentaries commentaries, Model model, HttpServletRequest request, @PathVariable("recipeId2") long recipeId) {
        int max_commentaries = 5;
        int current = 0;
        boolean moreThan5=false;
        HttpSession session = request.getSession();
        Long uid = (Long) session.getAttribute("id");
        Users user = usersRepo.findById(uid).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + uid));
        Recipes recipe = recipesRepo.findById(recipeId).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + recipeId));
        if(commentaries != null) {
            //Long commentarie_id = commentaries.getId();
            List<Commentaries> commentariesList = (List<Commentaries>) commentariesRepo.findAll();
            for (Commentaries commentaries1 : commentariesList) {
                if(commentaries1.getRecipes().getId()==recipeId){
                    current++;
                }
            }
            if(current < 5) {
                commentaries.setUsers(user);
                commentaries.setRecipes(recipe);
                commentariesRepo.save(commentaries);
            }else{
                moreThan5=true;
            }

        }
        System.out.println(moreThan5);
        model.addAttribute("isMoreThan5", moreThan5);
        return new RedirectView("/recipe/"+recipeId);
    }

    @PostMapping(value = "/recipe/addRate/{recipeId}")
    public RedirectView addRate(@Valid Ratings ratings, Model model, HttpServletRequest request, @PathVariable("recipeId") long recipeId){
        HttpSession session = request.getSession();
        Long uid = (Long) session.getAttribute("id");
        Recipes recipes = recipesRepo.findById(recipeId).orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe Id:" + recipeId));
        Users userDl = usersRepo.findById(uid).orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe Id:" + uid));
        List<Ratings> ratingsList = (List<Ratings>) ratingsRepo.findAll();
        Ratings rating=null;
        for(Ratings ratings1:ratingsList){
            if(ratings1.getUsers().getId()==userDl.getId() && ratings1.getRecipes().getId()==recipes.getId()){
                rating=ratings1;
            }
        }

        if(rating != null) {
            rating.setRate(ratings.getRate());
            ratingsRepo.save(rating);
        } else {
            ratings.setUsers(userDl);
            ratings.setRecipes(recipes);
            ratingsRepo.save(ratings);
        }

        model.addAttribute("recipes", recipes);

        return new RedirectView("/recipe/"+recipeId);
    }

    @PostMapping("/recipes")
    public RedirectView addRecipe(@Valid Recipes recipes, Model model, @RequestParam("img") MultipartFile file, @RequestParam(value = "category_new", required = false) String category_new, HttpServletRequest request){
        HttpSession session = request.getSession();
        //System.out.println("-----------------------"+session.getAttribute("isLogin")+" "+session.getAttribute("nickname"));

        Long uid = (Long) session.getAttribute("id");
        //System.out.println("---Drugi test"+uid);
        Users userDl=usersRepo.findById(uid).orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe Id:" + uid));

        recipes.setUser(userDl);

        if(!category_new.equals("")){
            Categories categories=new Categories();
            categories.setName(category_new);
            categoriesRepo.save(categories);
            recipes.setCategories(categories);
            recipesRepo.save(recipes);
        } else {
            recipesRepo.save(recipes);
        }
        if (!file.isEmpty()) {
            try {
                Images photo=new Images();
                // Get the file and save it somewhere
                byte[] bytes = file.getBytes();
                photo.setRecipes(recipes);
                photo.setFile_name(file.getOriginalFilename());
                imagesRepo.save(photo);
                Path path = Paths.get(uploadDirectory + file.getOriginalFilename());
                Files.write(path, bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("recipes", recipesRepo.findAll());
        model.addAttribute("categories", categoriesRepo.findAll());
        model.addAttribute("images", imagesRepo.findAll());
        model.addAttribute("users", usersRepo.findAll());
        return new RedirectView("/recipes");
    }

    @GetMapping("/modPanel")
    public String modPanel(Users users, Model model,HttpServletRequest request) {
        model.addAttribute("session", request.getSession().getAttribute("isLogin"));
        model.addAttribute("recipes", recipesRepo.findAll());
        model.addAttribute("categories", categoriesRepo.findAll());
        return "modPanel";
    }

    @PostMapping("/modPanel/deleteRecipe")
    public RedirectView deleteRecipe(Model model,@RequestParam(value = "recipe_id", required = true) int recipe_id ){
        Recipes recipes = recipesRepo.findById((long) recipe_id).orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe Id:" + recipe_id));

        List<Images> photos=recipes.getImages();
        for(Images photo:photos){
            File file1=new File(uploadDirectory+photo.getFile_name());
            file1.delete();
            imagesRepo.delete(photo);
        }
        List<Ratings> ratings=ratingsRepo.findByRecipesId(recipes.getId());
        for(Ratings ratings1:ratings){
            ratingsRepo.delete(ratings1);
        }

        List<Commentaries> commentariesList=commentariesRepo.findByRecipesId(recipes.getId());
        for(Commentaries commentaries:commentariesList){
            commentariesRepo.delete(commentaries);
        }

        recipesRepo.delete(recipes);

        return new RedirectView("/modPanel");
    }

    @PostMapping("/modPanel/deleteCategory")
    public String deleteCategory(Model model, @RequestParam(value = "category_id", required = true) int category_id, RedirectAttributes redirectAttributes){
        Categories categories = categoriesRepo.findById((long) category_id).orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe Id:" + category_id));
        List<Recipes> recipes=recipesRepo.findByCategoriesId(category_id);
        if(recipes.size()>0){
            //model.addAttribute("warning", true);
            redirectAttributes.addFlashAttribute("warning",true);
            return "redirect:/modPanel";
        } else {
            categoriesRepo.delete(categories);
            redirectAttributes.addFlashAttribute("warning",false);
            return "redirect:/modPanel";
        }
    }

    @GetMapping("/findRecipe/{id}")
    public ResponseEntity<?> getRecipe(@PathVariable("id")Long id){
        Optional<Recipes> l = recipesRepo.findById(id);
        List<Categories> c =  (List<Categories>) categoriesRepo.findAll();
        List<Object> test = new ArrayList<>();
        if(l.isPresent()){
            if(id!=0) {
                test.add(l.get());
                test.add(l.get().getCategories());
                test.add(l.get().getImages());
                test.add(c);
                return new ResponseEntity<>(test, HttpStatus.OK);
            }
        }
        return null;
    }

    @PostMapping("/modPanel/editRecipe")
    public RedirectView editRecipe(Model model, @RequestParam(value = "recipe_name", required=true) String recipe_name,
                                   @RequestParam(value = "category_id", required = true) long category_id,
                                   @RequestParam(value = "prepare_time", required = true) int prepare_time,
                                   @RequestParam(value = "ingredients", required = true) String ingredients,
                                   @RequestParam(value = "description", required = true) String description,
                                   @RequestParam("img") MultipartFile file,
                                   @RequestParam(value = "recipe_id", required = true) long recipe_id,
                                   @RequestParam(value = "photo_id", required = true) long photo_id
    ) throws IOException {
        System.out.println(0);
        Recipes recipes=recipesRepo.findById(recipe_id).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + recipe_id));
        recipes.setName(recipe_name);
        Categories categories=categoriesRepo.findById(category_id).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + category_id));
        recipes.setCategories(categories);
        recipes.setPrepare_time(prepare_time);
        recipes.setIngredients(ingredients);
        recipes.setDescription(description);
        System.out.println(1);
        if(!file.isEmpty()){
            Images images=imagesRepo.findById(photo_id).orElseThrow(()-> new IllegalArgumentException("Nieprawidłowe Id:" + photo_id));
            File file1 = new File(uploadDirectory + images.getFile_name());
            file1.delete();
            System.out.println(2);
            byte[] bytes = file.getBytes();
            images.setFile_name(file.getOriginalFilename());
            imagesRepo.save(images);
            Path path = Paths.get(uploadDirectory + file.getOriginalFilename());
            Files.write(path, bytes);
            System.out.println(3);
        }
        recipesRepo.save(recipes);
        System.out.println(4);

        return new RedirectView("/modPanel");
    }


    @PostMapping(value="/modPanel/addCategory")
    public RedirectView addCategory(Model model, @RequestParam(value = "category_name", required = true) String category_name){
        Categories categories=new Categories();
        categories.setName(category_name);
        categoriesRepo.save(categories);
        return new RedirectView("/modPanel");
    }


    int partition(List <Recipes> recipes, List<Integer> ratings, int low, int high)
    {
        int pivot = ratings.get(high);
        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++)
        {
            // If current element is smaller than the pivot
            if (ratings.get(j) < pivot)
            {
                i++;

                // swap arr[i] and arr[j]
                Recipes tempRecipe=recipes.get(i);
                int temp = ratings.get(i);
                recipes.set(i,recipes.get(j));
                ratings.set(i,ratings.get(j));
                recipes.set(j,tempRecipe);
                ratings.set(j,temp);
            }
        }

        // swap arr[i+1] and arr[high] (or pivot)
        Recipes tempRecipe=recipes.get(i+1);
        int temp = ratings.get(i+1);
        recipes.set(i+1,recipes.get(high));
        ratings.set(i+1,ratings.get(high));
        recipes.set(high,tempRecipe);
        ratings.set(high,temp);

        return i+1;
    }


    /* The main function that implements QuickSort()
      arr[] --> Array to be sorted,
      low  --> Starting index,
      high  --> Ending index */
    void sort(List <Recipes> recipes, List<Integer> ratings, int low, int high)
    {
        if (low < high)
        {
            /* pi is partitioning index, arr[pi] is
              now at right place */
            int pi = partition(recipes,ratings, low, high);

            // Recursively sort elements before
            // partition and after partition
            sort(recipes,ratings, low, pi-1);
            sort(recipes,ratings, pi+1, high);
        }
    }
}
