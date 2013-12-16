package com.companyx.android.appx;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.companyx.android.appx.RecipeDatabase.Recipe;
import com.companyx.android.appx.RecipeDatabase.RecipeDirection;
import com.companyx.android.appx.RecipeDatabase.RecipeIngredient;

/**
 * Recipe Activity
 * 
 * TODO Step-by-Step Walkthrough
 * 
 *  @author James Chin <jameslchin@gmail.com>
 */
public class RecipeActivity extends BaseActivity {
	// DEFAULT SETTINGS
	public static final byte MAX_QUANTITY = 8;
	
	// VIEW HOLDERS
	private LinearLayout layoutBody;
	private TextView textViewName;
	private ImageButton buttonFavorite;
	private Spinner spinnerShoppingList;
	
	// STATE VARIABLES
	private int recipeId;
	private Recipe recipe;
	
	// SYSTEM
	private RecipeDatabase recipeDatabase;
	private SharedPreferences.Editor sharedPrefEditor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}
	
	private void initialize() {
		recipeDatabase = RecipeDatabase.getInstance();
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		
		recipeId = getIntent().getIntExtra("recipeId", -1);
		recipe = recipeDatabase.findRecipeById(recipeId);
		
		// RECIPE NAME
		textViewName = (TextView) findViewById(R.id.textview_recipe_name);
		textViewName.setText(recipe.name);
		
		// FAVORITE BUTTON
		buttonFavorite = (ImageButton) findViewById(R.id.imagebutton_recipe_favorite);
		if (recipeDatabase.isFavorite(recipeId)) {
			buttonFavorite.setBackgroundResource(R.drawable.button_background_amber);
		} else {
			buttonFavorite.setBackgroundResource(R.drawable.button_background_gray);
		}
		buttonFavorite.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				synchronized(recipeDatabase) {
					if (recipeDatabase.isFavorite(recipeId)) {
						recipeDatabase.removeFavorite(recipeId);
						buttonFavorite.setBackgroundResource(R.drawable.button_background_gray);
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_favorites_removed) + " " + recipe.name + " " + getString(R.string.recipe_favorites_from_favorites), Toast.LENGTH_SHORT).show();
					} else {
						recipeDatabase.addFavorite(recipeId);
						buttonFavorite.setBackgroundResource(R.drawable.button_background_amber);
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_favorites_added) + " " + recipe.name + " " + getString(R.string.recipe_favorites_to_favorites), Toast.LENGTH_SHORT).show();
					}
					
					sharedPrefEditor.putString("SERIALIZED_FAVORITES", recipeDatabase.getSerializedFavorites());
					sharedPrefEditor.commit();
				}
			}
		});
		
		// SHOPPING LIST SPINNER
		spinnerShoppingList = (Spinner) findViewById(R.id.spinner_recipe_shopping_list);
		List<Byte> spinnerChoices = new ArrayList<Byte>();
		for (byte i = 0; i <= MAX_QUANTITY; i++)
			spinnerChoices.add(i);
		ArrayAdapter<Byte> shoppingListAdapter = new ArrayAdapter<Byte>(this, R.layout.spinner_item, spinnerChoices);
		shoppingListAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		spinnerShoppingList.setAdapter(shoppingListAdapter);
		spinnerShoppingList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				synchronized(recipeDatabase) {
					byte currentQty = recipeDatabase.getQuantity(recipeId);
					
					recipeDatabase.updateQuantity(recipeId, (byte) position); 
					sharedPrefEditor.putString("SERIALIZED_SHOPPING_LIST", recipeDatabase.getSerializedShoppingList());
					sharedPrefEditor.commit();
					
					if (position > currentQty)
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_shopping_list_added) + " " + (position - currentQty) + " " + recipe.name + " " + getString(R.string.recipe_shopping_list_to_shopping_list), Toast.LENGTH_SHORT).show();
					else if (position < currentQty)
						Toast.makeText(getApplicationContext(), getString(R.string.recipe_shopping_list_removed) + " " + (currentQty - position) + " " + recipe.name + " " + getString(R.string.recipe_shopping_list_from_shopping_list), Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinnerShoppingList.setSelection(recipeDatabase.getQuantity(recipeId), true);
		
		// TODO
		layoutBody = (LinearLayout) findViewById(R.id.layout_recipe_body);
		
		for (RecipeIngredient ri : recipe.ingredients) {
			TextView tv = new TextView(this);
			String s = ri.amount + " " + ri.measurement + " " + ri.ingredientName;
			tv.setText(s);
			layoutBody.addView(tv);
		}
		
		if (recipe.directions != null) {
			for (RecipeDirection rd : recipe.directions) {
				TextView tv = new TextView(this);
				String s = rd.direction;
				tv.setText(s);
				layoutBody.addView(tv);
			}
		}
	}
}
